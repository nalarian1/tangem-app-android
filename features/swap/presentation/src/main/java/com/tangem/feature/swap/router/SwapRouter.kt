package com.tangem.feature.swap.router

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

internal class SwapRouter(
    private val fragmentManager: WeakReference<FragmentManager>,
    private val customTabsManager: CustomTabsManager,
) {

    var currentScreen by mutableStateOf(SwapNavScreen.Main)
        private set

    fun openScreen(screen: SwapNavScreen) {
        currentScreen = screen
    }

    fun back() {
        if (currentScreen == SwapNavScreen.SelectToken) {
            currentScreen = SwapNavScreen.Main
        } else {
            fragmentManager.get()?.popBackStack()
        }
    }

    fun openUrl(url: String) {
        customTabsManager.openUrl(url)
    }
}

enum class SwapNavScreen {
    Main, Success, SelectToken
}