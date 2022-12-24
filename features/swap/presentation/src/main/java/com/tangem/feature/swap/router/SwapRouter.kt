package com.tangem.feature.swap.router

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

internal class SwapRouter(
    private val fragmentManager: WeakReference<FragmentManager>,
) {

    var currentScreen by mutableStateOf(SwapScreen.Main)
        private set

    fun openScreen(screen: SwapScreen) {
        currentScreen = screen
    }

    fun back() {
        if (currentScreen == SwapScreen.SelectToken) {
            currentScreen = SwapScreen.Main
        } else {
            fragmentManager.get()?.popBackStack()
        }
    }
}

enum class SwapScreen {
    Main, Success, SelectToken
}