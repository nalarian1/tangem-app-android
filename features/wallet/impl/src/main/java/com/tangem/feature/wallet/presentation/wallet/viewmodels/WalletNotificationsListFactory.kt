package com.tangem.feature.wallet.presentation.wallet.viewmodels

import com.tangem.common.Provider
import com.tangem.domain.common.CardTypesResolver
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.tokens.model.NetworkGroup
import com.tangem.domain.tokens.model.TokenList
import com.tangem.feature.wallet.presentation.common.state.TokenItemState
import com.tangem.feature.wallet.presentation.wallet.state.WalletMultiCurrencyState
import com.tangem.feature.wallet.presentation.wallet.state.WalletState
import com.tangem.feature.wallet.presentation.wallet.state.components.WalletNotification
import com.tangem.feature.wallet.presentation.wallet.state.components.WalletTokensListState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Wallet notifications list factory
 *
 * @property wasCardScannedCallback         callback that check if card was scanned
 * @property isUserAlreadyRateAppCallback   callback that check if card is user already rate app
 * @property isDemoCardCallback             callback that check if card is demo
 * @property clickIntents                   screen click intents
 *
 * @author Andrew Khokhlov on 16/07/2023
 */
internal class WalletNotificationsListFactory(
    private val currentStateProvider: Provider<WalletState>,
    private val wasCardScannedCallback: suspend (String) -> Boolean,
    private val isUserAlreadyRateAppCallback: suspend () -> Boolean,
    private val isDemoCardCallback: (String) -> Boolean,
    private val clickIntents: WalletClickIntents,
) {

    fun create(cardTypesResolver: CardTypesResolver, tokenList: TokenList?): Flow<ImmutableList<WalletNotification>> {
        // TODO: https://tangem.atlassian.net/browse/AND-4107 order
        return flow {
            emit(
                buildList {
                    if (cardTypesResolver.isTestCard()) {
                        add(element = WalletNotification.TestCard)
                        return@buildList
                    }

                    addRemainingSignaturesLeftNotifications(cardTypesResolver)

                    val isDemo = isDemoCardCallback(cardTypesResolver.getCardId())
                    if (!cardTypesResolver.isReleaseFirmwareType()) {
                        add(element = WalletNotification.DevCard)
                    } else {
                        addReleaseSpecialNotifications(cardTypesResolver = cardTypesResolver, isDemo = isDemo)
                    }

                    if (isDemo) {
                        add(element = WalletNotification.DemoCard)
                    }

                    if (hasUnreachableNetworks()) {
                        add(element = WalletNotification.UnreachableNetworks)
                    }

                    if (!cardTypesResolver.isBackupForbidden() && !cardTypesResolver.hasBackup()) {
                        add(element = WalletNotification.BackupCard(onClick = clickIntents::onBackupCardClick))
                    }

                    if (tokenList != null && tokenList.hasMissedDerivations()) {
                        add(element = WalletNotification.ScanCard(onClick = clickIntents::onScanCardClick))
                    }

                    if (isUserAlreadyRateAppCallback()) {
                        add(element = WalletNotification.LikeTangemApp(onClick = clickIntents::onLikeTangemAppClick))
                    }
                }.toImmutableList(),
            )
        }
    }

    private fun MutableList<WalletNotification>.addRemainingSignaturesLeftNotifications(
        cardTypesResolver: CardTypesResolver,
    ) {
        val remainingSignatures = cardTypesResolver.getRemainingSignatures()
        if (remainingSignatures != null && remainingSignatures <= MAX_REMAINING_SIGNATURES_COUNT) {
            add(element = WalletNotification.RemainingSignaturesLeft(remainingSignatures))
        }
    }

    private suspend fun MutableList<WalletNotification>.addReleaseSpecialNotifications(
        cardTypesResolver: CardTypesResolver,
        isDemo: Boolean,
    ) {
        if (!wasCardScannedCallback(cardTypesResolver.getCardId()) && cardTypesResolver.isMultiwalletAllowed() &&
            !isDemo
        ) {
            if (cardTypesResolver.isBackupForbidden() && cardTypesResolver.hasWalletSignedHashes()) {
                add(
                    element = WalletNotification.CriticalWarningAlreadySignedHashes(
                        onClick = clickIntents::onCriticalWarningAlreadySignedHashesClick,
                    ),
                )
            } else if (cardTypesResolver.hasWalletSignedHashes()) {
                add(
                    element = WalletNotification.WarningAlreadySignedHashes(
                        onClick = clickIntents::onCloseWarningAlreadySignedHashesClick,
                    ),
                )
            }
        }

        if (cardTypesResolver.isAttestationFailed()) {
            add(element = WalletNotification.CardVerificationFailed)
        }
    }

    private fun hasUnreachableNetworks(): Boolean {
        val isUnreachableState = { item: WalletTokensListState.TokensListItemState ->
            (item as? WalletTokensListState.TokensListItemState.Token)?.state is TokenItemState.Unreachable
        }

        return currentStateProvider().let { state ->
            state is WalletMultiCurrencyState.Content &&
                state.tokensListState is WalletTokensListState.ContentState &&
                state.tokensListState.items.any(isUnreachableState)
        }
    }

    private fun TokenList.hasMissedDerivations(): Boolean {
        val statuses = when (this) {
            is TokenList.GroupedByNetwork -> groups.flatMap(NetworkGroup::currencies).map(CryptoCurrencyStatus::value)
            is TokenList.Ungrouped -> currencies.map(CryptoCurrencyStatus::value)
            TokenList.NotInitialized -> emptyList()
        }

        return statuses.any { it is CryptoCurrencyStatus.MissedDerivation }
    }

    private companion object {
        const val MAX_REMAINING_SIGNATURES_COUNT = 10
    }
}