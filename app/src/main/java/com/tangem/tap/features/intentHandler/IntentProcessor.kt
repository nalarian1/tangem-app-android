package com.tangem.tap.features.intentHandler

import android.content.Intent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Anton Zhilenkov on 04.06.2023.
 */
// TODO: fixme: close it with the combined interfaces IntentHandler and IntentHandlerHolder
class IntentProcessor {

    private val intentHandlers = CopyOnWriteArrayList<IntentHandler>()

    fun addHandler(handler: IntentHandler) {
        intentHandlers.add(handler)
    }

    fun removeAll() {
        intentHandlers.clear()
    }

    fun handleIntent(intent: Intent?, isFromForeground: Boolean) {
        intentHandlers.forEach {
            it.handleIntent(intent, isFromForeground)
        }
    }
}
