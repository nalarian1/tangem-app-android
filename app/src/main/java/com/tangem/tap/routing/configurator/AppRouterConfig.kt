package com.tangem.tap.routing.configurator

import com.tangem.common.routing.AppRoute
import com.tangem.core.decompose.navigation.Router
import com.tangem.tap.common.SnackbarHandler
import kotlinx.coroutines.CoroutineScope

internal interface AppRouterConfig {

    var routerScope: CoroutineScope?
    var componentRouter: Router?
    var stack: List<AppRoute>?

    // TODO: Replace with UI message handler: https://tangem.atlassian.net/browse/AND-8005
    var snackbarHandler: SnackbarHandler?
}
