package com.tangem.tap.features.onboarding.products.otherCards.redux

import com.tangem.tap.features.wallet.redux.Artwork
import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 23/09/2021.
 */
sealed class OnboardingOtherCardsAction : Action {
    // from user, ui
    object LoadCardArtwork : OnboardingOtherCardsAction()
    object CreateWallet : OnboardingOtherCardsAction()

    // from redux
    class SetArtworkUrl(val artwork: Artwork) : OnboardingOtherCardsAction()
    object DetermineStepOfScreen : OnboardingOtherCardsAction()
    object Done : OnboardingOtherCardsAction()

    data class SetStepOfScreen(val step: OnboardingOtherCardsStep) : OnboardingOtherCardsAction()

    sealed class Confetti {
        object Show : OnboardingOtherCardsAction()
        object Hide : OnboardingOtherCardsAction()
    }
}