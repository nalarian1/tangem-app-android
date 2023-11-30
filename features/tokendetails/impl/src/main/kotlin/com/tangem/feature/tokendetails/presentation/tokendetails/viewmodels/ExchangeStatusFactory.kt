package com.tangem.feature.tokendetails.presentation.tokendetails.viewmodels

import arrow.core.getOrElse
import com.tangem.common.Provider
import com.tangem.domain.appcurrency.model.AppCurrency
import com.tangem.domain.tokens.GetCryptoCurrencyStatusesSyncUseCase
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.domain.wallets.usecase.GetSelectedWalletSyncUseCase
import com.tangem.feature.swap.domain.SwapRepository
import com.tangem.feature.swap.domain.SwapTransactionRepository
import com.tangem.feature.swap.domain.models.domain.ExchangeStatus
import com.tangem.feature.swap.domain.models.domain.ExchangeStatusModel
import com.tangem.feature.swap.domain.models.domain.SavedSwapTransactionListModel
import com.tangem.feature.tokendetails.presentation.tokendetails.state.SwapTransactionsState
import com.tangem.feature.tokendetails.presentation.tokendetails.state.factory.TokenDetailsSwapTransactionsStateConverter
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@Suppress("LongParameterList")
internal class ExchangeStatusFactory(
    private val swapTransactionRepository: SwapTransactionRepository,
    private val swapRepository: SwapRepository,
    private val getSelectedWalletSyncUseCase: GetSelectedWalletSyncUseCase,
    private val getMultiCryptoCurrencyStatusUseCase: GetCryptoCurrencyStatusesSyncUseCase,
    private val dispatchers: CoroutineDispatcherProvider,
    private val clickIntents: TokenDetailsClickIntents,
    private val appCurrencyProvider: Provider<AppCurrency>,
    private val userWalletId: UserWalletId,
    private val cryptoCurrencyId: CryptoCurrency.ID,
) {

    private val swapTransactionsStateConverter by lazy {
        TokenDetailsSwapTransactionsStateConverter(
            clickIntents = clickIntents,
            appCurrencyProvider = appCurrencyProvider,
        )
    }

    operator fun invoke() = combine(
        flow = swapTransactionRepository.getTransactions(userWalletId, cryptoCurrencyId),
        flow2 = getWalletCryptoCurrencies().conflate(),
    ) { savedTransactions, cryptoCurrenciesStatusList ->
        innerLoadSwapState(
            savedTransactions,
            cryptoCurrenciesStatusList,
        )
    }

    private fun getWalletCryptoCurrencies() = flow {
        val selectedWallet = getSelectedWalletSyncUseCase().fold(
            ifLeft = { null },
            ifRight = { it },
        )
        requireNotNull(selectedWallet) { "No selected wallet" }

        val cryptoCurrenciesList = getMultiCryptoCurrencyStatusUseCase(selectedWallet.walletId)
            .getOrElse { emptyList() }

        emit(cryptoCurrenciesList)
    }

    suspend fun updateSwapTxStatuses(swapTxList: PersistentList<SwapTransactionsState>) = withContext(dispatchers.io) {
        swapTxList.map { tx ->
            async {
                val status = getExchangeStatus(tx.txId)?.status
                swapTransactionsStateConverter.updateTxStatus(tx, status)
                tx.removeIfFinished(status)
            }
        }
            .awaitAll()
            .filterNotNull()
            .toPersistentList()
    }

    private suspend fun innerLoadSwapState(
        savedTransactions: List<SavedSwapTransactionListModel>?,
        cryptoCurrenciesStatusList: List<CryptoCurrencyStatus>,
    ): PersistentList<SwapTransactionsState> {
        val txWithStatuses = savedTransactions?.map { currencySwaps ->
            currencySwaps.copy(
                transactions = currencySwaps.transactions.map { tx ->
                    val status = getExchangeStatus(tx.txId)
                    tx.copy(
                        status = status,
                    )
                },
            )
        }

        return getExchangeStatusState(
            savedTransactions = txWithStatuses,
            cryptoCurrencyStatusList = cryptoCurrenciesStatusList,
        )
    }

    private suspend fun getExchangeStatus(txId: String): ExchangeStatusModel? {
        return swapRepository.getExchangeStatus(txId)
            .fold(
                ifLeft = { null },
                ifRight = { it },
            )
    }

    private fun getExchangeStatusState(
        savedTransactions: List<SavedSwapTransactionListModel>?,
        cryptoCurrencyStatusList: List<CryptoCurrencyStatus>,
    ): PersistentList<SwapTransactionsState> {
        if (savedTransactions == null || cryptoCurrencyStatusList.isEmpty()) {
            return persistentListOf()
        }

        return swapTransactionsStateConverter.convert(
            savedTransactions = savedTransactions,
            cryptoStatusList = cryptoCurrencyStatusList,
        )
    }

    private suspend fun SwapTransactionsState.removeIfFinished(status: ExchangeStatus?): SwapTransactionsState? {
        return if (status == ExchangeStatus.Refunded || status == ExchangeStatus.Finished) {
            swapTransactionRepository.removeTransaction(
                userWalletId = userWalletId,
                fromCryptoCurrencyId = fromCryptoCurrencyId,
                toCryptoCurrencyId = toCryptoCurrencyId,
                txId = txId,
            )
            null
        } else {
            this
        }
    }
}
