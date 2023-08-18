package com.tangem.feature.wallet.presentation.wallet.state.factory

import arrow.core.Either
import com.tangem.common.Provider
import com.tangem.domain.appcurrency.model.AppCurrency
import com.tangem.domain.common.CardTypesResolver
import com.tangem.domain.tokens.error.TokenListError
import com.tangem.domain.tokens.model.TokenList
import com.tangem.feature.wallet.presentation.wallet.state.WalletMultiCurrencyState
import com.tangem.feature.wallet.presentation.wallet.state.WalletState
import com.tangem.feature.wallet.presentation.wallet.state.factory.WalletLoadedTokensListConverter.LoadedTokensListModel
import com.tangem.feature.wallet.presentation.wallet.utils.TokenListErrorConverter
import com.tangem.feature.wallet.presentation.wallet.utils.TokenListToWalletStateConverter
import com.tangem.feature.wallet.presentation.wallet.viewmodels.WalletClickIntents
import com.tangem.utils.converter.Converter

/**
 * Converter from loaded [TokenListError] or [TokenList] to [WalletMultiCurrencyState]
 *
 * @property currentStateProvider  current ui state provider
 * @param cardTypeResolverProvider card type resolver
 * @param isLockedWalletProvider   current wallet is locked or not provider
 * @param clickIntents             screen click intents
 *
 * @author Andrew Khokhlov on 25/07/2023
 */
internal class WalletLoadedTokensListConverter(
    private val currentStateProvider: Provider<WalletState>,
    appCurrencyProvider: Provider<AppCurrency>,
    cardTypeResolverProvider: Provider<CardTypesResolver>,
    isLockedWalletProvider: Provider<Boolean>,
    clickIntents: WalletClickIntents,
) : Converter<LoadedTokensListModel, WalletMultiCurrencyState.Content> {

    private val tokenListStateConverter = TokenListToWalletStateConverter(
        currentStateProvider = currentStateProvider,
        cardTypeResolverProvider = cardTypeResolverProvider,
        isLockedWalletProvider = isLockedWalletProvider,
        appCurrencyProvider = appCurrencyProvider,
        isWalletContentHidden = false, // TODO: https://tangem.atlassian.net/browse/AND-4007
        clickIntents = clickIntents,
    )

    private val tokenListErrorStateConverter = TokenListErrorConverter(
        currentStateProvider = currentStateProvider,
    )

    override fun convert(value: LoadedTokensListModel): WalletMultiCurrencyState.Content {
        return value.tokenListEither.fold(
            ifLeft = tokenListErrorStateConverter::convert,
            ifRight = {
                tokenListStateConverter.convert(
                    value = TokenListToWalletStateConverter.TokensListModel(
                        tokenList = it,
                        isRefreshing = value.isRefreshing,
                    ),
                )
            },
        )
    }

    data class LoadedTokensListModel(
        val tokenListEither: Either<TokenListError, TokenList>,
        val isRefreshing: Boolean,
    )
}