package com.tangem.features.onramp.settings

import com.tangem.core.decompose.factory.ComponentFactory
import com.tangem.core.ui.decompose.ComposableContentComponent
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.wallets.models.UserWalletId

internal interface OnrampSettingsComponent : ComposableContentComponent {

    data class Params(
        val userWalletId: UserWalletId,
        val cryptoCurrency: CryptoCurrency,
        val onBack: () -> Unit,
    )

    interface Factory : ComponentFactory<Params, OnrampSettingsComponent>
}
