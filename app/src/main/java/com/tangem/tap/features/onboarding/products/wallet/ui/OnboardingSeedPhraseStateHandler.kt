package com.tangem.tap.features.onboarding.products.wallet.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.windowsize.rememberWindowSize
import com.tangem.feature.onboarding.api.OnboardingSeedPhraseScreen
import com.tangem.feature.onboarding.api.OnboardingSeedPhraseApi
import com.tangem.feature.onboarding.presentation.wallet2.viewmodel.SeedPhraseScreen
import com.tangem.feature.onboarding.presentation.wallet2.viewmodel.SeedPhraseViewModel
import com.tangem.tap.common.extensions.hide
import com.tangem.tap.common.extensions.show
import com.tangem.tap.features.onboarding.products.wallet.redux.OnboardingWalletState
import com.tangem.tap.features.onboarding.products.wallet.redux.OnboardingWalletStep
import com.tangem.wallet.R

/**
 * Created by Anton Zhilenkov on 20.10.2022.
 */
internal class OnboardingSeedPhraseStateHandler(
    private val onboardingSeedPhraseApi: OnboardingSeedPhraseApi = OnboardingSeedPhraseScreen(),
    private val activity: Activity,
) {

    fun newState(
        walletFragment: OnboardingWalletFragment,
        state: OnboardingWalletState,
        seedPhraseViewModel: SeedPhraseViewModel,
    ) {
        if (state.step == OnboardingWalletStep.CreateWallet && !seedPhraseViewModel.isFinished) {
            switchToWallet2SeedPhraseOnboarding(walletFragment, seedPhraseViewModel, state.getMaxProgress())
        } else {
            switchToWalletOnboarding(walletFragment, state)
        }
    }

    private fun switchToWalletOnboarding(walletFragment: OnboardingWalletFragment, state: OnboardingWalletState) {
        walletFragment.bindingSeedPhrase.onboardingSeedPhraseContainer.hide()
        walletFragment.pbBinding.pbState.show()
        walletFragment.binding.onboardingWalletContainer.show()
        walletFragment.handleOnboardingStep(state)
    }

    private fun switchToWallet2SeedPhraseOnboarding(
        walletFragment: OnboardingWalletFragment,
        viewModel: SeedPhraseViewModel,
        onboardingWalletMaxProgress: Int,
    ) {
        walletFragment.pbBinding.pbState.hide()
        walletFragment.binding.onboardingWalletContainer.hide()
        walletFragment.bindingSeedPhrase.onboardingSeedPhraseContainer.show()
        walletFragment.bindingSeedPhrase.onboardingSeedPhraseContainer.setContent {
            val subScreen = viewModel.currentScreen.collectAsState().value
            setMainScreenToolbarTitle(walletFragment, subScreen)

            TangemTheme(
                isDark = isSystemInDarkTheme(),
                windowSize = rememberWindowSize(activity = activity),
            ) {
                onboardingSeedPhraseApi.ScreenContent(
                    uiState = viewModel.uiState,
                    subScreen = subScreen,
                    progress = viewModel.progress.collectAsState(0).value.toFloat() / onboardingWalletMaxProgress,
                )
            }
        }
    }

    private fun setMainScreenToolbarTitle(walletFragment: OnboardingWalletFragment, subScreen: SeedPhraseScreen) {
        val titleResId = when (subScreen) {
            SeedPhraseScreen.Intro -> R.string.wallet_title
            SeedPhraseScreen.AboutSeedPhrase,
            SeedPhraseScreen.YourSeedPhrase,
            SeedPhraseScreen.CheckSeedPhrase,
            -> R.string.onboarding_create_wallet_header
            SeedPhraseScreen.ImportSeedPhrase -> R.string.onboarding_seed_intro_button_import
        }

        walletFragment.binding.toolbar.title = walletFragment.getString(titleResId)
    }
}
