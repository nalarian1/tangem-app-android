package com.tangem.tap.features.demo

import com.tangem.common.extensions.guard
import com.tangem.domain.common.extensions.makePrimaryWalletManager
import com.tangem.domain.common.extensions.withMainContext
import com.tangem.domain.demo.DemoConfig
import com.tangem.domain.models.scan.ScanResponse
import com.tangem.tap.common.entities.ProgressState
import com.tangem.tap.common.extensions.inject
import com.tangem.tap.domain.model.Currency
import com.tangem.tap.features.onboarding.products.note.redux.OnboardingNoteAction
import com.tangem.tap.proxy.redux.DaggerGraphState
import com.tangem.tap.scope
import com.tangem.tap.store
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 22/02/2022.
 */
internal class DemoOnboardingNoteMiddleware : DemoMiddleware {

    override fun tryHandle(config: DemoConfig, scanResponse: ScanResponse, action: Action): Boolean {
        val noteState = store.state.onboardingNoteState

        return when (action) {
            is OnboardingNoteAction.Balance.Update -> {
                val walletManager = if (noteState.walletManager != null) {
                    noteState.walletManager
                } else {
                    val wmFactory = runBlocking {
                        store.inject(DaggerGraphState::blockchainSDKFactory).getWalletManagerFactorySync()
                    }
                    val walletManager = wmFactory?.makePrimaryWalletManager(scanResponse).guard {
                        return false
                    }
                    store.dispatch(OnboardingNoteAction.SetWalletManager(walletManager))
                    walletManager
                }

                val balanceAmount = config.getBalance(walletManager.wallet.blockchain)
                val loadedBalance = noteState.walletBalance.copy(
                    value = balanceAmount.value!!,
                    currency = Currency.Blockchain(walletManager.wallet.blockchain, null),
                    state = ProgressState.Done,
                    error = null,
                    criticalError = null,
                )

                walletManager.wallet.setAmount(balanceAmount)

                scope.launch {
                    withMainContext {
                        store.dispatch(OnboardingNoteAction.Balance.Set(loadedBalance))
                        store.dispatch(OnboardingNoteAction.Balance.SetCriticalError(loadedBalance.criticalError))
                        store.dispatch(OnboardingNoteAction.Balance.SetNonCriticalError(loadedBalance.error))
                    }
                }
                return true
            }
            else -> false
        }
    }
}
