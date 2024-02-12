package com.tangem.managetokens.presentation.router

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import com.tangem.core.navigation.AppScreen
import com.tangem.features.managetokens.navigation.ManageTokensRouter

internal interface InnerManageTokensRouter : ManageTokensRouter {

    /**
     * Initialize router
     **/
    @Suppress("TopLevelComposableFunctions")
    @Composable
    fun Initialize(viewModelStoreOwner: ViewModelStoreOwner)

    /** Pop back stack */
    fun popBackStack(screen: AppScreen? = null)

    /** Open manage tokens screen */
    fun openManageTokensScreen()

    /** Open add custom token screen */
    fun openAddCustomTokenScreen()

    /** Open custom token choose network screen */
    fun openCustomTokenChooseNetwork()

    /** Open custom token choose derivation screen */
    fun openCustomTokenChooseDerivation()

    /** Open custom token choose wallet screen */
    fun openCustomTokenChooseWallet()
}
