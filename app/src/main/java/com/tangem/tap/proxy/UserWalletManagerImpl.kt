package com.tangem.tap.proxy

import com.tangem.blockchain.common.AmountType
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.DerivationParams
import com.tangem.blockchain.common.DerivationStyle
import com.tangem.blockchain.common.Token
import com.tangem.blockchain.common.WalletManager
import com.tangem.blockchain.common.WalletManagerFactory
import com.tangem.domain.common.CardDTO
import com.tangem.domain.common.TapWorkarounds.derivationStyle
import com.tangem.domain.common.extensions.fromNetworkId
import com.tangem.domain.common.extensions.toCoinId
import com.tangem.domain.common.extensions.toNetworkId
import com.tangem.lib.crypto.UserWalletManager
import com.tangem.lib.crypto.models.Currency
import com.tangem.lib.crypto.models.Currency.NativeToken
import com.tangem.lib.crypto.models.Currency.NonNativeToken
import com.tangem.lib.crypto.models.ProxyAmount
import com.tangem.lib.crypto.models.ProxyFiatCurrency
import com.tangem.tap.common.extensions.dispatchOnMain
import com.tangem.tap.domain.extensions.makeWalletManagerForApp
import com.tangem.tap.domain.model.builders.UserWalletIdBuilder
import com.tangem.tap.domain.tokens.models.BlockchainNetwork
import com.tangem.tap.features.wallet.redux.WalletAction
import kotlinx.coroutines.delay
import org.rekotlin.Action
import java.math.BigDecimal

class UserWalletManagerImpl(
    private val appStateHolder: AppStateHolder,
    private val walletManagerFactory: WalletManagerFactory,
) : UserWalletManager {

    override suspend fun getUserTokens(networkId: String, isExcludeCustom: Boolean): List<Currency> {
        val card = appStateHolder.getActualCard()
        val userTokensRepository =
            requireNotNull(appStateHolder.userTokensRepository) { "userTokensRepository is null" }
        return if (card != null) {
            userTokensRepository.getUserTokens(card)
                .filter {
                    val checkCustom = if (isExcludeCustom) {
                        !it.isCustomCurrency(null)
                    } else {
                        true
                    }
                    it.blockchain.toNetworkId() == networkId && checkCustom
                }
                .map {
                    if (it is com.tangem.tap.features.wallet.models.Currency.Token) {
                        NonNativeToken(
                            id = it.coinId ?: "",
                            name = it.currencyName,
                            symbol = it.currencySymbol,
                            networkId = it.blockchain.toNetworkId(),
                            contractAddress = it.token.contractAddress,
                            decimalCount = it.token.decimals,
                        )
                    } else {
                        NativeToken(
                            id = it.coinId ?: "",
                            name = it.currencyName,
                            symbol = it.currencySymbol,
                            networkId = it.blockchain.toNetworkId(),
                        )
                    }
                }
        } else {
            emptyList()
        }
    }

    override fun getNativeTokenForNetwork(networkId: String): Currency {
        val blockchain = requireNotNull(Blockchain.fromNetworkId(networkId)) { "blockchain not found" }
        return NativeToken(
            id = blockchain.toCoinId(),
            name = blockchain.fullName,
            symbol = blockchain.currency,
            networkId = networkId,
        )
    }

    override fun getWalletId(): String {
        return appStateHolder.getActualCard()?.let {
            UserWalletIdBuilder.card(it)
                .build()
                ?.stringValue
        }
            ?: ""
    }

    override suspend fun isTokenAdded(currency: Currency): Boolean {
        val card = requireNotNull(appStateHolder.getActualCard()) { "card is null" }
        val blockchain = requireNotNull(Blockchain.fromNetworkId(currency.networkId)) { "blockchain not found" }
        val blockchainNetwork = BlockchainNetwork(blockchain, card)
        val walletManager = appStateHolder.walletState?.getWalletManager(blockchainNetwork)
        if (walletManager != null) {
            return walletManager.cardTokens.any {
                it.id == currency.id
            }
        }
        return false
    }

    override suspend fun addToken(currency: Currency) {
        val card = requireNotNull(appStateHolder.getActualCard()) { "card not found" }
        val blockchain = requireNotNull(Blockchain.fromNetworkId(currency.networkId)) { "blockchain not found" }
        val blockchainNetwork = BlockchainNetwork(blockchain, card)
        val walletManager = getOrCreateBlockchain(blockchainNetwork, blockchain)
        if (currency is NonNativeToken &&
            !walletManager.cardTokens.contains(currency.toSdkToken())
        ) {
            val action = addNonNativeTokenToWalletAction(currency, card, blockchain)
            val mainStore = requireNotNull(appStateHolder.mainStore) { "mainStore is null" }
            mainStore.dispatchOnMain(action)
            delay(DELAY_UPDATE_WALLET)
        }
    }

    override fun getWalletAddress(networkId: String): String {
        val blockchain = requireNotNull(Blockchain.fromNetworkId(networkId)) { "blockchain not found" }
        val card = requireNotNull(appStateHolder.getActualCard()) { "card not found" }
        val blockchainNetwork = BlockchainNetwork(blockchain, card)
        val walletManager = appStateHolder.walletState?.getWalletManager(blockchainNetwork)
        if (walletManager != null) {
            return walletManager.wallet.address
        } else {
            error("no wallet manager found")
        }
    }

    override fun getLastTransactionHash(networkId: String): String? {
        val blockchain = requireNotNull(Blockchain.fromNetworkId(networkId)) { "blockchain not found" }
        val card = requireNotNull(appStateHolder.getActualCard()) { "card not found" }
        val blockchainNetwork = BlockchainNetwork(blockchain, card)
        val walletManager = appStateHolder.walletState?.getWalletManager(blockchainNetwork)
        return walletManager?.wallet?.recentTransactions?.lastOrNull()?.hash?.let { HEX_PREFIX + it }
    }

    override suspend fun getCurrentWalletTokensBalance(
        networkId: String,
        extraTokens: List<Currency>,
    ): Map<String, ProxyAmount> {
        val blockchain = requireNotNull(Blockchain.fromNetworkId(networkId)) { "blockchain not found" }
        val walletManager = getActualWalletManager(blockchain)

        // workaround for get balance for tokens that doesn't exist in wallet
        val extraTokensToLoadBalance = extraTokens
            .filterIsInstance<NonNativeToken>()
            .map {
                it.toSdkToken()
            }
            .filter {
                !walletManager.cardTokens.contains(it)
            }
        walletManager.addTokens(extraTokensToLoadBalance)
        walletManager.update()
        val balances = walletManager.wallet.amounts.map { entry ->
            val amount = entry.value
            amount.currencySymbol to ProxyAmount(
                amount.currencySymbol,
                amount.value ?: BigDecimal.ZERO,
                amount.decimals,
            )
        }.toMap()
        extraTokensToLoadBalance.forEach { walletManager.removeToken(it) }
        return balances
    }

    override fun getNativeTokenBalance(networkId: String): ProxyAmount? {
        val blockchain = requireNotNull(Blockchain.fromNetworkId(networkId)) { "blockchain not found" }
        val walletManager = getActualWalletManager(blockchain)
        return walletManager.wallet.amounts.firstNotNullOfOrNull {
            it.takeIf { it.key is AmountType.Coin }
        }?.value?.let {
            ProxyAmount(
                it.currencySymbol,
                it.value ?: BigDecimal.ZERO,
                it.decimals,
            )
        }
    }

    override fun getNetworkCurrency(networkId: String): String {
        val blockchain = requireNotNull(Blockchain.fromNetworkId(networkId)) { "blockchain not found" }
        return blockchain.currency
    }

    override fun getUserAppCurrency(): ProxyFiatCurrency {
        val appCurrency = appStateHolder.appFiatCurrency
        return ProxyFiatCurrency(
            code = appCurrency.code,
            name = appCurrency.name,
            symbol = appCurrency.symbol,
        )
    }

    private suspend fun getOrCreateBlockchain(
        blockchainNetwork: BlockchainNetwork,
        blockchain: Blockchain,
    ): WalletManager {
        val card = requireNotNull(appStateHolder.getActualCard()) { "card not found" }
        val scanResponse = requireNotNull(appStateHolder.scanResponse) { "scanResponse not found" }
        var walletManager = appStateHolder.walletState?.getWalletManager(blockchainNetwork)
        if (walletManager == null) {
            walletManager = walletManagerFactory.makeWalletManagerForApp(
                scanResponse = scanResponse,
                blockchain = blockchain,
                derivationParams = createDerivationParams(card.derivationStyle),
            )
            val action = WalletAction.MultiWallet.AddBlockchain(
                blockchain = blockchainNetwork,
                walletManager = walletManager,
                save = true,
            )
            val mainStore = requireNotNull(appStateHolder.mainStore) { "mainStore is null" }
            mainStore.dispatchOnMain(action)
            // workaround to wait until blockchain adds to walletStores and update appStateHolder.walletState
            delay(DELAY_UPDATE_WALLET)
        }

        return requireNotNull(walletManager) { "cant create walletManager" }
    }

    private fun addNonNativeTokenToWalletAction(token: NonNativeToken, card: CardDTO, blockchain: Blockchain): Action {
        return WalletAction.MultiWallet.AddToken(
            token = Token(
                id = token.id,
                name = token.name,
                symbol = token.symbol,
                contractAddress = token.contractAddress,
                decimals = token.decimalCount,
            ),
            blockchain = BlockchainNetwork(
                blockchain,
                card,
            ),
            save = true,
        )
    }

    override fun refreshWallet() {
        // workaround, should update wallet after transaction
        appStateHolder.mainStore?.dispatchOnMain(WalletAction.LoadData.Refresh)
    }

    private fun createDerivationParams(derivationStyle: DerivationStyle?): DerivationParams? {
        // todo clarify if its need to add Custom
        return derivationStyle?.let { DerivationParams.Default(derivationStyle) }
    }

    private fun getActualWalletManager(blockchain: Blockchain): WalletManager {
        val card = requireNotNull(appStateHolder.getActualCard()) { "card not found" }
        val blockchainNetwork = BlockchainNetwork(blockchain, card)
        return requireNotNull(appStateHolder.walletState?.getWalletManager(blockchainNetwork)) {
            "No wallet manager found"
        }
    }

    companion object {
        private const val HEX_PREFIX = "0x"
        private const val DELAY_UPDATE_WALLET = 500L
    }
}

private fun NonNativeToken.toSdkToken(): Token {
    return Token(
        id = this.id,
        name = this.name,
        symbol = this.symbol,
        contractAddress = this.contractAddress,
        decimals = this.decimalCount,
    )
}
