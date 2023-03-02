package com.tangem.feature.swap.domain

import com.tangem.feature.swap.domain.cache.SwapDataCache
import com.tangem.feature.swap.domain.converters.CryptoCurrencyConverter
import com.tangem.feature.swap.domain.models.DataError
import com.tangem.feature.swap.domain.models.SwapAmount
import com.tangem.feature.swap.domain.models.domain.ApproveModel
import com.tangem.feature.swap.domain.models.domain.Currency
import com.tangem.feature.swap.domain.models.domain.PreparedSwapConfigState
import com.tangem.feature.swap.domain.models.domain.SwapDataModel
import com.tangem.feature.swap.domain.models.toStringWithRightOffset
import com.tangem.feature.swap.domain.models.ui.AmountFormatter
import com.tangem.feature.swap.domain.models.ui.FoundTokensState
import com.tangem.feature.swap.domain.models.ui.PermissionDataState
import com.tangem.feature.swap.domain.models.ui.PreselectTokens
import com.tangem.feature.swap.domain.models.ui.RequestApproveStateData
import com.tangem.feature.swap.domain.models.ui.SwapState
import com.tangem.feature.swap.domain.models.ui.TokenBalanceData
import com.tangem.feature.swap.domain.models.ui.TokenSwapInfo
import com.tangem.feature.swap.domain.models.ui.TokenWithBalance
import com.tangem.feature.swap.domain.models.ui.TokensDataState
import com.tangem.feature.swap.domain.models.ui.TxState
import com.tangem.lib.crypto.TransactionManager
import com.tangem.lib.crypto.UserWalletManager
import com.tangem.lib.crypto.models.ProxyFiatCurrency
import com.tangem.lib.crypto.models.transactions.SendTxResult
import com.tangem.utils.toFiatString
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@Suppress("LargeClass")
internal class SwapInteractorImpl @Inject constructor(
    private val transactionManager: TransactionManager,
    private val userWalletManager: UserWalletManager,
    private val repository: SwapRepository,
    private val cache: SwapDataCache,
    private val allowPermissionsHandler: AllowPermissionsHandler,
) : SwapInteractor {

    private val cryptoCurrencyConverter = CryptoCurrencyConverter()
    private val amountFormatter = AmountFormatter()

    override suspend fun initTokensToSwap(initialCurrency: Currency): TokensDataState {
        val networkId = initialCurrency.networkId
        val availableTokens = cache.getAvailableTokens(networkId)
        val allLoadedTokens = availableTokens.ifEmpty {
            val tokens = repository.getExchangeableTokens(networkId)
            cache.cacheAvailableToSwapTokens(networkId, tokens)
            tokens
        }.filter { it.symbol != initialCurrency.symbol }

        // replace tokens in wallet tokens list with loaded same
        val loadedOnWalletsMap = mutableSetOf<String>()
        val tokensInWallet = userWalletManager.getUserTokens(networkId = networkId, isExcludeCustom = true)
            .filter { it.symbol != initialCurrency.symbol }
            .map { token ->
                allLoadedTokens.firstOrNull { it.symbol == token.symbol }?.let {
                    loadedOnWalletsMap.add(it.symbol)
                    it
                } ?: cryptoCurrencyConverter.convertBack(token)
            }
        val loadedTokens = allLoadedTokens
            .filter {
                !loadedOnWalletsMap.contains(it.symbol)
            }
        val tokensBalance = userWalletManager.getCurrentWalletTokensBalance(networkId, emptyList())
            .mapValues { SwapAmount(it.value.value, it.value.decimals) }
        val appCurrency = userWalletManager.getUserAppCurrency()
        val rates = repository.getRates(appCurrency.code, tokensInWallet.map { it.id })
        cache.cacheBalances(tokensBalance)
        cache.cacheLoadedTokens(loadedTokens.map { TokenWithBalance(it) })
        cache.cacheInWalletTokens(getTokensWithBalance(tokensInWallet, tokensBalance, rates, appCurrency))
        return TokensDataState(
            preselectTokens = PreselectTokens(
                fromToken = initialCurrency,
                toToken = selectToToken(initialCurrency, tokensInWallet, loadedTokens),
            ),
            foundTokensState = FoundTokensState(
                tokensInWallet = cache.getInWalletTokens(),
                loadedTokens = cache.getLoadedTokens(),
            ),
        )
    }

    override suspend fun searchTokens(networkId: String, searchQuery: String): FoundTokensState {
        val searchQueryLowerCase = searchQuery.lowercase()
        val tokensInWallet = cache.getInWalletTokens()
            .filter {
                it.token.name.lowercase().contains(searchQueryLowerCase) ||
                    it.token.symbol.lowercase().contains(searchQueryLowerCase)
            }
        val loadedTokens = cache.getLoadedTokens()
            .filter {
                it.token.name.lowercase().contains(searchQueryLowerCase) ||
                    it.token.symbol.lowercase().contains(searchQueryLowerCase)
            }
        return FoundTokensState(
            tokensInWallet = tokensInWallet,
            loadedTokens = loadedTokens,
        )
    }

    override fun findTokenById(id: String): Currency? {
        val tokensInWallet = cache.getInWalletTokens()
        val loadedTokens = cache.getLoadedTokens()
        return tokensInWallet.firstOrNull { it.token.id == id }?.token
            ?: loadedTokens.firstOrNull { it.token.id == id }?.token
    }

    override suspend fun givePermissionToSwap(
        networkId: String,
        estimatedGas: Int,
        transactionData: ApproveModel,
        forTokenContractAddress: String,
    ): TxState {
        val increasedEstimatedGas = increaseByPercents(TWENTY_FIVE_PERCENTS, estimatedGas)
        val gasPrice = transactionData.gasPrice.toBigDecimalOrNull() ?: error("cannot parse gasPrice")
        val fee = transactionManager.calculateFee(networkId, gasPrice.toPlainString(), increasedEstimatedGas)
        val result = transactionManager.sendApproveTransaction(
            networkId = networkId,
            feeAmount = fee,
            estimatedGas = estimatedGas,
            destinationAddress = transactionData.toAddress,
            dataToSign = transactionData.data,
        )
        return when (result) {
            is SendTxResult.Success -> {
                allowPermissionsHandler.addAddressToInProgress(forTokenContractAddress)
                TxState.TxSent(txAddress = userWalletManager.getLastTransactionHash(networkId) ?: "")
            }
            SendTxResult.UserCancelledError -> TxState.UserCancelled
            is SendTxResult.BlockchainSdkError -> TxState.BlockchainError
            is SendTxResult.TangemSdkError -> TxState.TangemSdkError
            is SendTxResult.NetworkError -> TxState.NetworkError
            is SendTxResult.UnknownError -> TxState.UnknownError
        }
    }

    override suspend fun findBestQuote(
        networkId: String,
        fromToken: Currency,
        toToken: Currency,
        amountToSwap: String,
    ): SwapState {
        syncWalletBalanceForTokens(networkId, listOf(fromToken, toToken))
        val amountDecimal = toBigDecimalOrNull(amountToSwap)
        if (amountDecimal == null || amountDecimal.compareTo(BigDecimal.ZERO) == 0) {
            return createEmptyAmountState(fromToken, toToken)
        }
        val amount = SwapAmount(amountDecimal, getTokenDecimals(fromToken))
        val fromTokenAddress = getTokenAddress(fromToken)
        val toTokenAddress = getTokenAddress(toToken)
        val isAllowedToSpend = checkAllowance(networkId, fromTokenAddress)
        if (isAllowedToSpend && allowPermissionsHandler.isAddressAllowanceInProgress(fromTokenAddress)) {
            allowPermissionsHandler.removeAddressFromProgress(fromTokenAddress)
            transactionManager.updateWalletManager(networkId)
        }
        // load initial quotes data, it works despite balance
        val quotesData = loadQuoteData(
            networkId = networkId,
            fromTokenAddress = fromTokenAddress,
            toTokenAddress = toTokenAddress,
            amount = amount,
            fromToken = fromToken,
            toToken = toToken,
        )
        // get fee from loaded quotes data, if error, use blockchain fee for 0 amount tx
        val fee = getInchFee(quotesData)
        val isFeeEnough = checkFeeIsEnough(
            fee = fee,
            spendAmount = amount,
            networkId = networkId,
            fromToken = fromToken,
        )
        val isBalanceEnough = isBalanceEnough(fromToken, amount, fee)
        val preparedSwapConfigState = PreparedSwapConfigState(
            isAllowedToSpend = isAllowedToSpend,
            isBalanceEnough = isBalanceEnough,
            isFeeEnough = isFeeEnough,
        )
        return if (isAllowedToSpend && isBalanceEnough && isFeeEnough) {
            // if enough balance, fee and spend was allowed, request swap data
            val swapData = loadSwapData(
                networkId = networkId,
                fromTokenAddress = fromTokenAddress,
                toTokenAddress = toTokenAddress,
                fromToken = fromToken,
                toToken = toToken,
                amount = amount,
            )
            if (swapData is SwapState.QuotesLoadedState) {
                swapData.copy(preparedSwapConfigState = preparedSwapConfigState)
            } else {
                swapData
            }
        } else {
            if (quotesData is SwapState.QuotesLoadedState) {
                quotesData.copy(preparedSwapConfigState = preparedSwapConfigState)
            } else {
                quotesData
            }
        }
    }

    override suspend fun onSwap(
        networkId: String,
        swapData: SwapDataModel,
        currencyToSend: Currency,
        currencyToGet: Currency,
        amountToSwap: String,
    ): TxState {
        val amount = requireNotNull(toBigDecimalOrNull(amountToSwap)) { "wrong amount format, use only digits" }
        val estimatedGas = swapData.transaction.gas.toIntOrNull() ?: DEFAULT_GAS
        val fee = transactionManager.calculateFee(
            networkId = networkId,
            gasPrice = swapData.transaction.gasPrice,
            estimatedGas = estimatedGas,
        )
        val result = transactionManager.sendTransaction(
            networkId = networkId,
            amountToSend = amount,
            currencyToSend = cryptoCurrencyConverter.convert(currencyToSend),
            feeAmount = fee,
            estimatedGas = estimatedGas,
            destinationAddress = swapData.transaction.toWalletAddress,
            dataToSign = swapData.transaction.data,
            isSwap = true,
        )
        return when (result) {
            is SendTxResult.Success -> {
                userWalletManager.addToken(cryptoCurrencyConverter.convert(currencyToGet))
                userWalletManager.refreshWallet()
                TxState.TxSent(
                    fromAmount = amountFormatter.formatSwapAmountToUI(swapData.fromTokenAmount, currencyToSend.symbol),
                    toAmount = amountFormatter.formatSwapAmountToUI(swapData.toTokenAmount, currencyToGet.symbol),
                    txAddress = userWalletManager.getLastTransactionHash(networkId) ?: "",
                )
            }
            SendTxResult.UserCancelledError -> TxState.UserCancelled
            is SendTxResult.BlockchainSdkError -> TxState.BlockchainError
            is SendTxResult.TangemSdkError -> TxState.TangemSdkError
            is SendTxResult.NetworkError -> TxState.NetworkError
            is SendTxResult.UnknownError -> TxState.UnknownError
        }
    }

    override fun getTokenBalance(token: Currency): SwapAmount {
        return cache.getBalanceForToken(token.symbol) ?: SwapAmount(BigDecimal.ZERO, getTokenDecimals(token))
    }

    override fun isAvailableToSwap(networkId: String): Boolean {
        return ONE_INCH_SUPPORTED_NETWORKS.contains(networkId)
    }

    private fun getTangemFee(): Double {
        return repository.getTangemFee()
    }

    fun getTokenDecimals(token: Currency): Int {
        return if (token is Currency.NonNativeToken) {
            token.decimalCount
        } else {
            transactionManager.getNativeTokenDecimals(token.networkId)
        }
    }

    private fun getInchFee(quotesData: SwapState): BigDecimal? {
        return if (quotesData is SwapState.QuotesLoadedState) {
            quotesData.feeRaw
        } else null
    }

    private fun selectToToken(
        initialToken: Currency,
        tokensInWallet: List<Currency>,
        loadedTokens: List<Currency>,
    ): Currency {
        val toToken = if (tokensInWallet.isNotEmpty()) {
            tokensInWallet.firstOrNull { it.symbol != initialToken.symbol }
                ?: loadedTokens.first { it.symbol != initialToken.symbol }
        } else {
            val findUsdt = loadedTokens.firstOrNull { it.symbol == USDT_SYMBOL && it.symbol != initialToken.symbol }
            if (findUsdt == null) {
                val findUsdc = loadedTokens.firstOrNull { it.symbol == USDC_SYMBOL && it.symbol != initialToken.symbol }
                findUsdc ?: loadedTokens.first { it.symbol != initialToken.symbol }
            } else {
                findUsdt
            }
        }
        return toToken
    }

    private fun getTokensWithBalance(
        tokens: List<Currency>,
        balances: Map<String, SwapAmount>,
        rates: Map<String, Double>,
        appCurrency: ProxyFiatCurrency,
    ): List<TokenWithBalance> {
        return tokens.map {
            val balance = balances[it.symbol]
            TokenWithBalance(
                token = it,
                tokenBalanceData = TokenBalanceData(
                    amount = balance?.let { amount ->
                        amountFormatter.formatSwapAmountToUI(amount, it.symbol)
                    },
                    amountEquivalent = balance?.value?.toFiatString(
                        rateValue = rates[it.id]?.toBigDecimal() ?: BigDecimal.ZERO,
                        fiatCurrencyName = appCurrency.symbol,
                        formatWithSpaces = true,
                    ),
                ),
            )
        }
    }

    private suspend fun checkAllowance(networkId: String, fromTokenAddress: String): Boolean {
        val allowance = repository.checkTokensSpendAllowance(
            networkId = networkId,
            tokenAddress = fromTokenAddress,
            walletAddress = userWalletManager.getWalletAddress(networkId),
        )
        return allowance.error == DataError.NoError && allowance.dataModel != ZERO_BALANCE
    }

    private fun createEmptyAmountState(
        fromToken: Currency,
        toToken: Currency,
    ): SwapState {
        val appCurrency = userWalletManager.getUserAppCurrency()
        val fromTokenBalance = cache.getBalanceForToken(fromToken.symbol)
        val toTokenBalance = cache.getBalanceForToken(toToken.symbol)
        return SwapState.EmptyAmountState(
            fromTokenWalletBalance = fromTokenBalance?.let { amountFormatter.formatSwapAmountToUI(it, "") }.orEmpty(),
            toTokenWalletBalance = toTokenBalance?.let { amountFormatter.formatSwapAmountToUI(it, "") }.orEmpty(),
            zeroAmountEquivalent = BigDecimal.ZERO.toFiatString(
                rateValue = BigDecimal.ONE,
                fiatCurrencyName = appCurrency.symbol,
                formatWithSpaces = true,
            ),
        )
    }

    /**
     * Load quote data calls only if spend is not allowed for token contract address
     */
    @Suppress("LongParameterList")
    private suspend fun loadQuoteData(
        networkId: String,
        fromTokenAddress: String,
        toTokenAddress: String,
        amount: SwapAmount,
        fromToken: Currency,
        toToken: Currency,
    ): SwapState {
        repository.findBestQuote(
            networkId = networkId,
            fromTokenAddress = fromTokenAddress,
            toTokenAddress = toTokenAddress,
            amount = amount.toStringWithRightOffset(),
        ).let { quotes ->
            val quoteDataModel = quotes.dataModel
            if (quoteDataModel != null) {
                val transactionData = repository.dataToApprove(networkId, getTokenAddress(fromToken))
                val fee = transactionManager.calculateFee(
                    networkId = networkId,
                    estimatedGas = quoteDataModel.estimatedGas,
                    gasPrice = transactionData.gasPrice,
                )
                val feeFiat = getFormattedFiatFee(networkId, fromToken.id, toToken.id, fee)
                val formattedFee = amountFormatter.formatBigDecimalAmountToUI(
                    amount = fee,
                    decimals = transactionManager.getNativeTokenDecimals(networkId),
                    currency = userWalletManager.getNetworkCurrency(networkId),
                ) + feeFiat
                val swapState = updateBalances(
                    networkId = networkId,
                    fromToken = fromToken,
                    toToken = toToken,
                    fromTokenAmount = quoteDataModel.fromTokenAmount,
                    toTokenAmount = quoteDataModel.toTokenAmount,
                    formattedFee = formattedFee,
                    feeRaw = fee,
                    swapDataModel = null,
                )
                return updatePermissionState(
                    networkId = networkId,
                    fromToken = fromToken,
                    quotesLoadedState = swapState,
                    estimatedGas = quoteDataModel.estimatedGas,
                    transactionData = transactionData,
                    formattedFee = formattedFee,
                )
            } else {
                return SwapState.SwapError(quotes.error)
            }
        }
    }

    private suspend fun getFormattedFiatFee(
        networkId: String,
        fromTokenId: String,
        toTokenId: String,
        fee: BigDecimal,
    ): String {
        val appCurrency = userWalletManager.getUserAppCurrency()
        val nativeToken = userWalletManager.getNativeTokenForNetwork(networkId)
        val rates = repository.getRates(appCurrency.code, listOf(fromTokenId, toTokenId, nativeToken.id))
        return rates[nativeToken.id]?.toBigDecimal()?.let { rate ->
            " (${fee.toFiatString(rate, appCurrency.symbol, true)})"
        }.orEmpty()
    }

    /**
     * Load swap data calls only if spend is allowed for token contract address
     */
    @Suppress("LongParameterList")
    private suspend fun loadSwapData(
        networkId: String,
        fromTokenAddress: String,
        toTokenAddress: String,
        fromToken: Currency,
        toToken: Currency,
        amount: SwapAmount,
    ): SwapState {
        repository.prepareSwapTransaction(
            networkId = networkId,
            fromTokenAddress = fromTokenAddress,
            toTokenAddress = toTokenAddress,
            amount = amount.toStringWithRightOffset(),
            slippage = DEFAULT_SLIPPAGE,
            fromWalletAddress = getWalletAddress(networkId),
        ).let {
            val swapData = it.dataModel
            if (swapData != null) {
                val fee = transactionManager.calculateFee(
                    networkId = networkId,
                    estimatedGas = swapData.transaction.gas.toIntOrNull() ?: DEFAULT_GAS,
                    gasPrice = swapData.transaction.gasPrice,
                )
                val feeFiat = getFormattedFiatFee(networkId, fromToken.id, toToken.id, fee)
                val formattedFee = amountFormatter.formatBigDecimalAmountToUI(
                    amount = fee,
                    decimals = transactionManager.getNativeTokenDecimals(networkId),
                    currency = userWalletManager.getNetworkCurrency(networkId),
                ) + feeFiat
                val swapState = updateBalances(
                    networkId = networkId,
                    fromToken = fromToken,
                    toToken = toToken,
                    fromTokenAmount = swapData.fromTokenAmount,
                    toTokenAmount = swapData.toTokenAmount,
                    formattedFee = formattedFee,
                    swapDataModel = swapData,
                    feeRaw = fee,
                )
                return swapState.copy(
                    permissionState = PermissionDataState.Empty,
                )
            } else {
                return SwapState.SwapError(it.error)
            }
        }
    }

    @Suppress("LongParameterList")
    private suspend fun updateBalances(
        networkId: String,
        fromToken: Currency,
        toToken: Currency,
        fromTokenAmount: SwapAmount,
        toTokenAmount: SwapAmount,
        formattedFee: String,
        feeRaw: BigDecimal,
        swapDataModel: SwapDataModel?,
    ): SwapState.QuotesLoadedState {
        val appCurrency = userWalletManager.getUserAppCurrency()
        val nativeToken = userWalletManager.getNativeTokenForNetwork(networkId)
        val rates = repository.getRates(appCurrency.code, listOf(fromToken.id, toToken.id, nativeToken.id))
        val fromTokenBalance = cache.getBalanceForToken(fromToken.symbol)
        val toTokenBalance = cache.getBalanceForToken(toToken.symbol)
        return SwapState.QuotesLoadedState(
            fromTokenInfo = TokenSwapInfo(
                tokenAmount = fromTokenAmount,
                coinId = fromToken.id,
                tokenWalletBalance = fromTokenBalance?.let { amountFormatter.formatSwapAmountToUI(it, "") }
                    ?: ZERO_BALANCE,
                tokenFiatBalance = fromTokenAmount.value.toFiatString(
                    rateValue = rates[fromToken.id]?.toBigDecimal() ?: BigDecimal.ZERO,
                    fiatCurrencyName = appCurrency.symbol,
                    formatWithSpaces = true,
                ),
            ),
            toTokenInfo = TokenSwapInfo(
                tokenAmount = toTokenAmount,
                coinId = toToken.id,
                tokenWalletBalance = toTokenBalance?.let { amountFormatter.formatSwapAmountToUI(it, "") }
                    ?: ZERO_BALANCE,
                tokenFiatBalance = toTokenAmount.value.toFiatString(
                    rateValue = rates[toToken.id]?.toBigDecimal() ?: BigDecimal.ZERO,
                    fiatCurrencyName = appCurrency.symbol,
                    formatWithSpaces = true,
                ),
            ),
            fee = formattedFee,
            priceImpact = calculatePriceImpact(
                fromTokenAmount = fromTokenAmount.value,
                fromRate = rates[fromToken.id] ?: 0.0,
                toTokenAmount = toTokenAmount.value,
                toRate = rates[toToken.id] ?: 0.0,
            ),
            networkCurrency = userWalletManager.getNetworkCurrency(networkId),
            swapDataModel = swapDataModel,
            tangemFee = getTangemFee(),
            feeRaw = feeRaw,
        )
    }

    @Suppress("LongParameterList")
    private fun updatePermissionState(
        networkId: String,
        fromToken: Currency,
        quotesLoadedState: SwapState.QuotesLoadedState,
        estimatedGas: Int,
        transactionData: ApproveModel,
        formattedFee: String,
    ): SwapState.QuotesLoadedState {
        val isTokenZeroBalance = getTokenBalance(fromToken).value.compareTo(BigDecimal.ZERO) == 0
        if (isTokenZeroBalance) {
            return quotesLoadedState.copy(
                permissionState = PermissionDataState.Empty,
            )
        }
        if (allowPermissionsHandler.isAddressAllowanceInProgress(getTokenAddress(fromToken))) {
            return quotesLoadedState.copy(
                permissionState = PermissionDataState.PermissionLoading,
            )
        }
        return quotesLoadedState.copy(
            permissionState = PermissionDataState.PermissionReadyForRequest(
                currency = fromToken.symbol,
                amount = INFINITY_SYMBOL,
                walletAddress = getWalletAddress(networkId),
                spenderAddress = transactionData.toAddress,
                fee = formattedFee,
                requestApproveData = RequestApproveStateData(
                    estimatedGas = estimatedGas,
                    approveModel = transactionData,
                ),
            ),
        )
    }

    private suspend fun syncWalletBalanceForTokens(networkId: String, tokens: List<Currency>) {
        val tokensToSync = tokens.filter { cache.getBalanceForToken(it.symbol) == null }
        if (tokensToSync.isNotEmpty()) {
            val tokensBalance =
                userWalletManager.getCurrentWalletTokensBalance(
                    networkId = networkId,
                    extraTokens = tokensToSync.map { cryptoCurrencyConverter.convert(it) },
                )
            cache.cacheBalances(tokensBalance.mapValues { SwapAmount(it.value.value, it.value.decimals) })
        }
    }

    private fun isBalanceEnough(fromToken: Currency, amount: SwapAmount, fee: BigDecimal?): Boolean {
        val tokenBalance = getTokenBalance(fromToken).value
        return if (fromToken is Currency.NonNativeToken) {
            tokenBalance >= amount.value
        } else {
            tokenBalance > amount.value.plus(fee ?: BigDecimal.ZERO)
        }
    }

    private fun getWalletAddress(networkId: String): String {
        return userWalletManager.getWalletAddress(networkId)
    }

    private fun getTokenAddress(currency: Currency): String {
        return when (currency) {
            is Currency.NativeToken -> {
                DEFAULT_BLOCKCHAIN_INCH_ADDRESS
            }
            is Currency.NonNativeToken -> {
                currency.contractAddress
            }
        }
    }

    private fun checkFeeIsEnough(
        fee: BigDecimal?,
        spendAmount: SwapAmount,
        networkId: String,
        fromToken: Currency,
    ): Boolean {
        if (fee == null) {
            return false
        }
        val nativeTokenBalance = userWalletManager.getNativeTokenBalance(networkId)
        val percentsToFeeIncrease = BigDecimal.valueOf(INCREASE_FEE_TO_CHECK_ENOUGH_PERCENT)
        return when (fromToken) {
            is Currency.NativeToken -> {
                nativeTokenBalance?.let { balance ->
                    return balance.value.minus(spendAmount.value) > fee.multiply(percentsToFeeIncrease)
                } ?: false
            }
            is Currency.NonNativeToken -> {
                nativeTokenBalance?.let { balance ->
                    return balance.value > fee.multiply(percentsToFeeIncrease)
                } ?: false
            }
        }
    }

    @Suppress("MagicNumber")
    private fun increaseByPercents(percents: Int, value: Int): Int {
        return value * (percents / 100 + 1)
    }

    private fun toBigDecimalOrNull(amountToSwap: String): BigDecimal? {
        return amountToSwap.replace(",", ".").toBigDecimalOrNull()
    }

    private fun calculatePriceImpact(
        fromTokenAmount: BigDecimal,
        fromRate: Double,
        toTokenAmount: BigDecimal,
        toRate: Double,
    ): Float {
        val toTokenFiatValue = toTokenAmount.multiply(toRate.toBigDecimal())
        val fromTokenFiatValue = fromTokenAmount.multiply(fromRate.toBigDecimal())
        return (BigDecimal.ONE - toTokenFiatValue.divide(fromTokenFiatValue, 2, RoundingMode.HALF_UP)).toFloat()
    }

    companion object {
        private const val DEFAULT_SLIPPAGE = 2
        private const val ZERO_BALANCE = "0"
        private const val DEFAULT_GAS = 300000
        private const val DEFAULT_BLOCKCHAIN_INCH_ADDRESS = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
        private const val TWENTY_FIVE_PERCENTS = 25
        private const val INCREASE_FEE_TO_CHECK_ENOUGH_PERCENT = 1.4
        private const val USDT_SYMBOL = "USDT"
        private const val USDC_SYMBOL = "USDC"
        private const val INFINITY_SYMBOL = "∞"

        private val ONE_INCH_SUPPORTED_NETWORKS = listOf(
            "ethereum",
            "binance-smart-chain",
            "polygon-pos",
            "optimistic-ethereum",
            "arbitrum-one",
            "xdai",
            "avalanche",
            "fantom",
        )
    }
}
