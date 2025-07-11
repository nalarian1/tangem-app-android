package com.tangem.feature.wallet.navigation

import com.tangem.domain.wallets.models.UserWalletId
import kotlinx.serialization.Serializable

@Serializable
internal sealed class WalletRoute {

    @Serializable
    data object Wallet : WalletRoute()

    @Serializable
    data class OrganizeTokens(val userWalletId: UserWalletId) : WalletRoute()
}
