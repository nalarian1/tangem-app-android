package com.tangem.data.staking

import android.util.Base64
import arrow.core.raise.catch
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchainsdk.utils.toCoinId
import com.tangem.common.extensions.toCompressedPublicKey
import com.tangem.data.common.cache.CacheRegistry
import com.tangem.data.staking.converters.*
import com.tangem.data.staking.converters.action.ActionStatusConverter
import com.tangem.data.staking.converters.action.EnterActionResponseConverter
import com.tangem.data.staking.converters.action.StakingActionTypeConverter
import com.tangem.data.staking.converters.transaction.GasEstimateConverter
import com.tangem.data.staking.converters.transaction.StakingTransactionConverter
import com.tangem.data.staking.converters.transaction.StakingTransactionStatusConverter
import com.tangem.data.staking.converters.transaction.StakingTransactionTypeConverter
import com.tangem.datasource.api.common.response.getOrThrow
import com.tangem.datasource.api.stakekit.StakeKitApi
import com.tangem.datasource.api.stakekit.models.request.*
import com.tangem.datasource.api.stakekit.models.response.model.YieldBalanceWrapperDTO
import com.tangem.datasource.local.preferences.AppPreferencesStore
import com.tangem.datasource.local.preferences.PreferencesKeys
import com.tangem.datasource.local.preferences.utils.getObjectListSync
import com.tangem.datasource.local.token.StakingBalanceStore
import com.tangem.datasource.local.token.StakingYieldsStore
import com.tangem.domain.core.lce.LceFlow
import com.tangem.domain.core.lce.lceFlow
import com.tangem.domain.staking.model.StakingAvailability
import com.tangem.domain.staking.model.StakingEntryInfo
import com.tangem.domain.staking.model.UnsubmittedTransactionMetadata
import com.tangem.domain.staking.model.stakekit.NetworkType
import com.tangem.domain.staking.model.stakekit.Yield
import com.tangem.domain.staking.model.stakekit.YieldBalance
import com.tangem.domain.staking.model.stakekit.YieldBalanceList
import com.tangem.domain.staking.model.stakekit.action.StakingAction
import com.tangem.domain.staking.model.stakekit.action.StakingActionCommonType
import com.tangem.domain.staking.model.stakekit.action.StakingActionType
import com.tangem.domain.staking.model.stakekit.transaction.ActionParams
import com.tangem.domain.staking.model.stakekit.transaction.StakingGasEstimate
import com.tangem.domain.staking.model.stakekit.transaction.StakingTransaction
import com.tangem.domain.staking.repositories.StakingRepository
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.CryptoCurrencyAddress
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.walletmanager.WalletManagersFacade
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.features.staking.api.featuretoggles.StakingFeatureToggles
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import com.tangem.utils.extensions.orZero
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("LargeClass", "LongParameterList")
internal class DefaultStakingRepository(
    private val stakeKitApi: StakeKitApi,
    private val appPreferencesStore: AppPreferencesStore,
    private val stakingYieldsStore: StakingYieldsStore,
    private val stakingBalanceStore: StakingBalanceStore,
    private val cacheRegistry: CacheRegistry,
    private val dispatchers: CoroutineDispatcherProvider,
    private val stakingFeatureToggle: StakingFeatureToggles,
    private val walletManagersFacade: WalletManagersFacade,
) : StakingRepository {

    private val stakingNetworkTypeConverter = StakingNetworkTypeConverter()
    private val networkTypeConverter = StakingNetworkTypeConverter()
    private val transactionStatusConverter = StakingTransactionStatusConverter()
    private val transactionTypeConverter = StakingTransactionTypeConverter()
    private val actionStatusConverter = ActionStatusConverter()
    private val stakingActionTypeConverter = StakingActionTypeConverter()
    private val tokenConverter = TokenConverter(
        stakingNetworkTypeConverter = stakingNetworkTypeConverter,
    )
    private val yieldConverter = YieldConverter(
        tokenConverter = tokenConverter,
    )
    private val gasEstimateConverter = GasEstimateConverter(
        tokenConverter = tokenConverter,
    )
    private val transactionConverter = StakingTransactionConverter(
        networkTypeConverter = networkTypeConverter,
        transactionStatusConverter = transactionStatusConverter,
        transactionTypeConverter = transactionTypeConverter,
        gasEstimateConverter = gasEstimateConverter,
    )
    private val enterActionResponseConverter = EnterActionResponseConverter(
        actionStatusConverter = actionStatusConverter,
        stakingActionTypeConverter = stakingActionTypeConverter,
        transactionConverter = transactionConverter,
    )

    private val yieldBalanceConverter = YieldBalanceConverter()

    private val yieldBalanceListConverter = YieldBalanceListConverter()

    private val isYieldBalanceFetching = MutableStateFlow(
        value = emptyMap<UserWalletId, Boolean>(),
    )

    override fun isStakingSupported(currencyId: String): Boolean {
        return integrationIdMap.containsKey(currencyId)
    }

    override suspend fun fetchEnabledYields(refresh: Boolean) {
        withContext(dispatchers.io) {
            cacheRegistry.invokeOnExpire(
                key = YIELDS_STORE_KEY,
                skipCache = refresh,
                block = {
                    val stakingTokensWithYields = stakeKitApi.getMultipleYields().getOrThrow()
                    stakingYieldsStore.store(stakingTokensWithYields.data)
                },
            )
        }
    }

    override suspend fun getYield(cryptoCurrencyId: CryptoCurrency.ID, symbol: String): Yield {
        return withContext(dispatchers.io) {
            val yields = getEnabledYields() ?: error("No yields found")
            val rawCurrencyId = cryptoCurrencyId.rawCurrencyId ?: error("Staking custom tokens is not available")

            val prefetchedYield = findPrefetchedYield(
                yields = yields,
                currencyId = rawCurrencyId,
                symbol = symbol,
            )

            prefetchedYield ?: error("Staking is unavailable")
        }
    }

    override suspend fun getEntryInfo(cryptoCurrencyId: CryptoCurrency.ID, symbol: String): StakingEntryInfo {
        return withContext(dispatchers.io) {
            val yield = getYield(cryptoCurrencyId, symbol)

            StakingEntryInfo(
                interestRate = requireNotNull(yield.validators.maxByOrNull { it.apr.orZero() }?.apr),
                periodInDays = yield.metadata.cooldownPeriod.days,
                tokenSymbol = yield.token.symbol,
            )
        }
    }

    override suspend fun getStakingAvailabilityForActions(
        cryptoCurrencyId: CryptoCurrency.ID,
        symbol: String,
    ): StakingAvailability {
        val rawCurrencyId = cryptoCurrencyId.rawCurrencyId ?: return StakingAvailability.Unavailable

        return withContext(dispatchers.io) {
            val yields = getEnabledYields() ?: return@withContext StakingAvailability.Unavailable

            val prefetchedYield = findPrefetchedYield(yields, rawCurrencyId, symbol)
            val isSupported = isStakingSupported(rawCurrencyId)

            when {
                prefetchedYield != null && isSupported -> {
                    StakingAvailability.Available(prefetchedYield.id)
                }
                prefetchedYield == null && isSupported -> {
                    StakingAvailability.TemporaryDisabled
                }
                else -> StakingAvailability.Unavailable
            }
        }
    }

    override suspend fun createAction(
        userWalletId: UserWalletId,
        network: Network,
        params: ActionParams,
    ): StakingAction {
        return withContext(dispatchers.io) {
            val response = when (params.actionCommonType) {
                StakingActionCommonType.ENTER -> stakeKitApi.createEnterAction(
                    createActionRequestBody(
                        userWalletId,
                        network,
                        params,
                    ),
                )
                StakingActionCommonType.EXIT -> stakeKitApi.createExitAction(
                    createActionRequestBody(
                        userWalletId,
                        network,
                        params,
                    ),
                )
                StakingActionCommonType.PENDING_OTHER,
                StakingActionCommonType.PENDING_REWARDS,
                -> stakeKitApi.createPendingAction(
                    createPendingActionRequestBody(params),
                )
            }

            enterActionResponseConverter.convert(response.getOrThrow())
        }
    }

    override suspend fun estimateGas(
        userWalletId: UserWalletId,
        network: Network,
        params: ActionParams,
    ): StakingGasEstimate {
        return withContext(dispatchers.io) {
            val gasEstimateDTO = when (params.actionCommonType) {
                StakingActionCommonType.ENTER -> stakeKitApi.estimateGasOnEnter(
                    createActionRequestBody(
                        userWalletId,
                        network,
                        params,
                    ),
                )
                StakingActionCommonType.EXIT -> stakeKitApi.estimateGasOnExit(
                    createActionRequestBody(
                        userWalletId,
                        network,
                        params,
                    ),
                )
                StakingActionCommonType.PENDING_REWARDS,
                StakingActionCommonType.PENDING_OTHER,
                -> stakeKitApi.estimateGasOnPending(
                    createPendingActionRequestBody(params),
                )
            }

            gasEstimateConverter.convert(gasEstimateDTO.getOrThrow())
        }
    }

    override suspend fun constructTransaction(transactionId: String): StakingTransaction {
        return withContext(dispatchers.io) {
            val transactionResponse = stakeKitApi.constructTransaction(
                transactionId = transactionId,
                body = ConstructTransactionRequestBody(),
            )

            transactionConverter.convert(transactionResponse.getOrThrow())
        }
    }

    override suspend fun fetchSingleYieldBalance(
        userWalletId: UserWalletId,
        address: CryptoCurrencyAddress,
        refresh: Boolean,
    ) = withContext(dispatchers.io) {
        if (!stakingFeatureToggle.isStakingEnabled) return@withContext

        val cryptoCurrency = address.cryptoCurrency
        val rawCurrencyId =
            cryptoCurrency.id.rawCurrencyId ?: error("Staking custom tokens is not available")

        val integrationId = integrationIdMap[rawCurrencyId] ?: return@withContext

        cacheRegistry.invokeOnExpire(
            key = getYieldBalancesKey(userWalletId),
            skipCache = refresh,
            block = {
                val requestBody = getBalanceRequestData(address.address, integrationId)
                val result = stakeKitApi.getSingleYieldBalance(
                    integrationId = requestBody.integrationId,
                    body = requestBody,
                ).getOrThrow()

                stakingBalanceStore.store(
                    requestBody.integrationId,
                    YieldBalanceWrapperDTO(
                        balances = result,
                        integrationId = requestBody.integrationId,
                    ),
                )
            },
        )
    }

    override fun getSingleYieldBalanceFlow(
        userWalletId: UserWalletId,
        address: CryptoCurrencyAddress,
    ): Flow<YieldBalance> = channelFlow {
        if (!stakingFeatureToggle.isStakingEnabled) {
            send(YieldBalance.Empty)
        } else {
            launch(dispatchers.io) {
                val integrationId = integrationIdMap[address.cryptoCurrency.id.rawCurrencyId]
                    ?: error("Could not get integrationId")
                stakingBalanceStore.get(integrationId)
                    .collectLatest {
                        send(
                            yieldBalanceConverter.convert(
                                YieldBalanceConverter.Data(
                                    balance = it,
                                    integrationId = integrationId,
                                ),
                            ),
                        )
                    }
            }

            withContext(dispatchers.io) {
                fetchSingleYieldBalance(
                    userWalletId,
                    address,
                )
            }
        }
    }.cancellable()

    override suspend fun getSingleYieldBalanceSync(
        userWalletId: UserWalletId,
        address: CryptoCurrencyAddress,
    ): YieldBalance = withContext(dispatchers.io) {
        if (!stakingFeatureToggle.isStakingEnabled) {
            YieldBalance.Empty
        } else {
            fetchSingleYieldBalance(userWalletId, address)

            val integrationId = integrationIdMap[address.cryptoCurrency.id.rawCurrencyId]
                ?: error("Could not get integrationId")
            val result = stakingBalanceStore.getSyncOrNull(integrationId) ?: return@withContext YieldBalance.Error
            yieldBalanceConverter.convert(
                YieldBalanceConverter.Data(
                    balance = result,
                    integrationId = integrationId,
                ),
            )
        }
    }

    override suspend fun fetchMultiYieldBalance(
        userWalletId: UserWalletId,
        addresses: List<CryptoCurrencyAddress>,
        refresh: Boolean,
    ) = withContext(dispatchers.io) {
        if (!stakingFeatureToggle.isStakingEnabled) return@withContext
        try {
            isYieldBalanceFetching.update {
                it + (userWalletId to true)
            }
            cacheRegistry.invokeOnExpire(
                key = getYieldBalancesKey(userWalletId),
                skipCache = refresh,
                block = {
                    val result = stakeKitApi.getMultipleYieldBalances(
                        addresses
                            .mapNotNull { networkAddress ->
                                val cryptoCurrency = networkAddress.cryptoCurrency
                                val rawCurrencyId = cryptoCurrency.id.rawCurrencyId ?: error("Currency raw id is null")
                                val integrationId = integrationIdMap[rawCurrencyId]

                                if (integrationId != null) {
                                    networkAddress.address to integrationId
                                } else {
                                    null
                                }
                            }
                            .distinct()
                            .map { getBalanceRequestData(it.first, it.second) },
                    ).getOrThrow()

                    stakingBalanceStore.store(result)
                },
            )
        } finally {
            isYieldBalanceFetching.update {
                it - userWalletId
            }
        }
    }

    override fun getMultiYieldBalanceFlow(
        userWalletId: UserWalletId,
        addresses: List<CryptoCurrencyAddress>,
    ): Flow<YieldBalanceList> = channelFlow {
        if (!stakingFeatureToggle.isStakingEnabled) {
            send(YieldBalanceList.Empty)
        } else {
            launch(dispatchers.io) {
                stakingBalanceStore.get()
                    .collectLatest { send(yieldBalanceListConverter.convert(it)) }
            }

            withContext(dispatchers.io) {
                fetchMultiYieldBalance(
                    userWalletId,
                    addresses,
                )
            }
        }
    }.cancellable()

    override fun getMultiYieldBalanceLce(
        userWalletId: UserWalletId,
        addresses: List<CryptoCurrencyAddress>,
    ): LceFlow<Throwable, YieldBalanceList> = lceFlow {
        if (!stakingFeatureToggle.isStakingEnabled) {
            send(YieldBalanceList.Empty)
        } else {
            launch(dispatchers.io) {
                combine(
                    stakingBalanceStore.get(),
                    isYieldBalanceFetching.map { it.getOrElse(userWalletId) { false } },
                ) { result, isFetching ->
                    val balances = yieldBalanceListConverter.convert(result)
                    send(balances, isStillLoading = isFetching)
                }.collect()
            }
            withContext(dispatchers.io) {
                catch(
                    block = { fetchMultiYieldBalance(userWalletId, addresses, refresh = false) },
                    catch = { raise(it) },
                )
            }
        }
    }

    override suspend fun getMultiYieldBalanceSync(
        userWalletId: UserWalletId,
        addresses: List<CryptoCurrencyAddress>,
    ): YieldBalanceList = withContext(dispatchers.io) {
        if (!stakingFeatureToggle.isStakingEnabled) {
            YieldBalanceList.Empty
        } else {
            fetchMultiYieldBalance(userWalletId, addresses)
            val result = stakingBalanceStore.getSyncOrNull() ?: return@withContext YieldBalanceList.Error
            yieldBalanceListConverter.convert(result)
        }
    }

    override suspend fun submitHash(transactionId: String, transactionHash: String) {
        withContext(dispatchers.io) {
            stakeKitApi.submitTransactionHash(
                transactionId = transactionId,
                body = SubmitTransactionHashRequestBody(
                    hash = transactionHash,
                ),
            )
        }
    }

    override suspend fun storeUnsubmittedHash(unsubmittedTransactionMetadata: UnsubmittedTransactionMetadata) {
        withContext(dispatchers.io) {
            appPreferencesStore.editData { preferences ->
                val savedTransactions = preferences.getObjectListOrDefault<UnsubmittedTransactionMetadata>(
                    key = PreferencesKeys.UNSUBMITTED_TRANSACTIONS_KEY,
                    default = emptyList(),
                )

                preferences.setObjectList(
                    key = PreferencesKeys.UNSUBMITTED_TRANSACTIONS_KEY,
                    value = savedTransactions + unsubmittedTransactionMetadata,
                )
            }
        }
    }

    override suspend fun sendUnsubmittedHashes() {
        withContext(NonCancellable) {
            val savedTransactions = appPreferencesStore.getObjectListSync<UnsubmittedTransactionMetadata>(
                key = PreferencesKeys.UNSUBMITTED_TRANSACTIONS_KEY,
            )

            savedTransactions.forEach {
                stakeKitApi.submitTransactionHash(
                    transactionId = it.transactionId,
                    body = SubmitTransactionHashRequestBody(hash = it.transactionHash),
                )
            }

            appPreferencesStore.editData { mutablePreferences ->
                mutablePreferences.setObjectList<UnsubmittedTransactionMetadata>(
                    key = PreferencesKeys.UNSUBMITTED_TRANSACTIONS_KEY,
                    value = emptyList(),
                )
            }
        }
    }

    private suspend fun createActionRequestBody(
        userWalletId: UserWalletId,
        network: Network,
        params: ActionParams,
    ): ActionRequestBody {
        return ActionRequestBody(
            integrationId = params.integrationId,
            addresses = Address(
                address = params.address,
                additionalAddresses = createAdditionalAddresses(userWalletId, network, params),
            ),
            args = ActionRequestBodyArgs(
                amount = params.amount.toPlainString(),
                inputToken = tokenConverter.convertBack(params.token),
                validatorAddress = params.validatorAddress,
            ),
        )
    }

    private fun createPendingActionRequestBody(params: ActionParams): PendingActionRequestBody {
        return PendingActionRequestBody(
            integrationId = params.integrationId,
            type = params.type ?: StakingActionType.UNKNOWN,
            passthrough = params.passthrough.orEmpty(),
            args = ActionRequestBodyArgs(
                amount = params.amount.toPlainString(),
                validatorAddress = params.validatorAddress,
            ),
        )
    }

    private suspend fun createAdditionalAddresses(
        userWalletId: UserWalletId,
        network: Network,
        params: ActionParams,
    ): Address.AdditionalAddresses? {
        val selectedWallet = walletManagersFacade.getOrCreateWalletManager(userWalletId, network)
        return when (params.token.network) {
            NetworkType.COSMOS -> Address.AdditionalAddresses(
                cosmosPubKey = Base64.encodeToString(
                    /* input = */ selectedWallet?.wallet?.publicKey?.blockchainKey?.toCompressedPublicKey(),
                    /* flags = */ Base64.NO_WRAP,
                ),
            )
            else -> null
        }
    }

    override fun isStakeMoreAvailable(networkId: Network.ID): Boolean {
        val blockchain = Blockchain.fromId(networkId.value)
        return when (blockchain) {
            Blockchain.Solana -> false
            else -> true
        }
    }

    private fun findPrefetchedYield(yields: List<Yield>, currencyId: String, symbol: String): Yield? {
        return yields.find { it.token.coinGeckoId == currencyId && it.token.symbol == symbol }
    }

    private suspend fun getEnabledYields(): List<Yield>? {
        val yields = stakingYieldsStore.getSyncOrNull() ?: return null
        return yields.map { yieldConverter.convert(it) }
    }

    private fun getBalanceRequestData(address: String, integrationId: String): YieldBalanceRequestBody {
        return YieldBalanceRequestBody(
            addresses = Address(
                address = address,
                additionalAddresses = null, // todo fill additional addresses metadata if needed
                explorerUrl = "", // todo fill exporer url https://tangem.atlassian.net/browse/AND-7138
            ),
            args = YieldBalanceRequestBody.YieldBalanceRequestArgs(
                validatorAddresses = listOf(), // todo add validators https://tangem.atlassian.net/browse/AND-7138
            ),
            integrationId = integrationId,
        )
    }

    private fun getYieldBalancesKey(userWalletId: UserWalletId) = "yield_balance_${userWalletId.stringValue}"

    private companion object {
        const val YIELDS_STORE_KEY = "yields"

        const val SOLANA_INTEGRATION_ID = "solana-sol-native-multivalidator-staking"
        const val COSMOS_INTEGRATION_ID = "cosmos-atom-native-staking"
        const val POLKADOT_INTEGRATION_ID = "polkadot-dot-validator-staking"
        const val ETHEREUM_INTEGRATION_ID = "ethereum-matic-native-staking"
        const val AVALANCHE_INTEGRATION_ID = "avalanche-avax-native-staking"
        const val TRON_INTEGRATION_ID = "tron-trx-native-staking"
        const val CRONOS_INTEGRATION_ID = "cronos-cro-native-staking"
        const val BINANCE_INTEGRATION_ID = "bsc-bnb-native-staking"
        const val KAVA_INTEGRATION_ID = "kava-kava-native-staking"
        const val NEAR_INTEGRATION_ID = "near-near-native-staking"
        const val TEZOS_INTEGRATION_ID = "tezos-xtz-native-staking"

        // uncomment items as implementation is ready
        val integrationIdMap = mapOf(
            Blockchain.Solana.toCoinId() to SOLANA_INTEGRATION_ID,
            Blockchain.Cosmos.toCoinId() to COSMOS_INTEGRATION_ID,
            // Blockchain.Polkadot.toCoinId() to POLKADOT_INTEGRATION_ID,
            // Blockchain.Polygon.toCoinId() to ETHEREUM_INTEGRATION_ID,
            // Blockchain.Avalanche.toCoinId() to AVALANCHE_INTEGRATION_ID,
            // Blockchain.Tron.toCoinId() to TRON_INTEGRATION_ID,
            // Blockchain.Cronos.toCoinId() to CRONOS_INTEGRATION_ID,
            // Blockchain.Binance.toCoinId() to BINANCE_INTEGRATION_ID,
            // Blockchain.Kava.toCoinId() to KAVA_INTEGRATION_ID,
            // Blockchain.Near.toCoinId() to NEAR_INTEGRATION_ID,
            // Blockchain.Tezos.toCoinId() to TEZOS_INTEGRATION_ID,
        )
    }
}
