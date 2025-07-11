package com.tangem.core.deeplink.impl

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.tangem.core.deeplink.DEEPLINK_KEY
import com.tangem.core.deeplink.DeepLink
import com.tangem.core.deeplink.DeepLinksRegistry
import timber.log.Timber

internal class DefaultDeepLinksRegistry : DeepLinksRegistry {

    private var registries: List<DeepLink> = emptyList()
    private var lastDeepLink: Uri? = null

    override fun launch(intent: Intent): Boolean {
        // Try to get deeplink from data (direct deeplink flow)
        // Otherwise, try to get from extras (notification deeplink flow)
        val deepLinkExtras = intent.getStringExtra(DEEPLINK_KEY)?.toUri()
        val received = intent.data ?: deepLinkExtras ?: return false
        lastDeepLink = received
        var hasMatch = false

        Timber.i(
            """
                Received deep link intent
                |- Received URI: $received
                |- Registries: $registries
            """.trimIndent(),
        )
        registries.forEach { deepLink ->
            val expected = deepLink.uri.toUri()

            if (!isMatches(expected, received)) return@forEach
            hasMatch = true

            val params = getParams(expected, received)

            logMatch(hasMatch, expected, received, params)

            deepLink.onReceive(params)
            lastDeepLink = null // clear deeplink if it was handled
        }

        if (!hasMatch) {
            logMatch(hasMatch, null, received, null)
        }

        return hasMatch
    }

    override fun register(deepLinks: Collection<DeepLink>) {
        registries = (registries + deepLinks).distinctBy(DeepLink::id)

        Timber.d(
            """
                Registered deep links
                |- Registries: $registries
            """.trimIndent(),
        )
    }

    override fun register(deepLink: DeepLink) {
        registries = (registries + deepLink).distinctBy(DeepLink::id)

        Timber.d(
            """
                Registered deep link
                |- Registries: $registries
            """.trimIndent(),
        )
    }

    override fun unregister(deepLinks: Collection<DeepLink>) {
        registries = registries.filter { it !in deepLinks }

        Timber.d(
            """
                Unregistered deep links
                |- Registries: $registries
            """.trimIndent(),
        )
    }

    override fun unregister(deepLink: DeepLink) {
        registries = registries.filter { it.id != deepLink.id }

        Timber.d(
            """
                Unregistered deep link
                |- Registries: $registries
            """.trimIndent(),
        )
    }

    override fun unregisterByIds(ids: Collection<String>) {
        registries = registries.filter { it.id !in ids }

        Timber.d(
            """
                Unregistered deep links
                |- Registries: $registries
            """.trimIndent(),
        )
    }

    override fun triggerDelayedDeeplink(deepLinkClass: Class<out DeepLink>) {
        val received = lastDeepLink
        if (received != null) {
            var hasMatch = false
            registries
                .filterIsInstance(deepLinkClass)
                .forEach { deepLink ->
                    if (!deepLink.shouldHandleDelayed) return@forEach
                    val expected = deepLink.uri.toUri()
                    if (!isMatches(expected, received)) return@forEach
                    hasMatch = true

                    val params = getParams(expected, received)
                    logMatch(hasMatch, expected, received, params)
                    deepLink.onReceive(params)
                }

            if (!hasMatch) {
                logMatch(hasMatch, null, received, null)
            }
            lastDeepLink = null // clear deeplink in any case handle or not
        }
    }

    override fun cancelDelayedDeeplink() {
        lastDeepLink = null
    }

    private fun logMatch(hasMatch: Boolean, expected: Uri?, received: Uri?, params: Map<String, String>?) {
        if (hasMatch) {
            Timber.i(
                """
                    Matched deep link
                    |- Expected URI: $expected
                    |- Received URI: $received
                    |- Params: $params
                """.trimIndent(),
            )
        } else {
            Timber.i(
                """
                    No match found for deep link
                    |- Received URI: $received
                    |- Registries: $registries
                """.trimIndent(),
            )
        }
    }

    private fun isMatches(received: Uri, expected: Uri): Boolean {
        if (received == expected) return true
        if (received.authority != expected.authority ||
            received.pathSegments.size != expected.pathSegments.size
        ) {
            return false
        }

        received.pathSegments.forEachIndexed { index, receivedSegment ->
            val expectedSegment = expected.pathSegments[index]
            if (receivedSegment != expectedSegment &&
                !(receivedSegment.startsWith(prefix = "{") && receivedSegment.endsWith(suffix = "}"))
            ) {
                return false
            }
        }

        return true
    }

    private fun getParams(received: Uri, expected: Uri): Map<String, String> {
        val params = mutableMapOf<String, String>()

        received.pathSegments.forEachIndexed { index, receivedSegment ->
            val expectedSegment = expected.pathSegments[index]
            if (receivedSegment != expectedSegment &&
                receivedSegment.startsWith(prefix = "{") &&
                receivedSegment.endsWith(suffix = "}")
            ) {
                val path = receivedSegment
                    .replace(oldValue = "{", newValue = "")
                    .replace(oldValue = "}", newValue = "")

                params[path] = expectedSegment
            }
        }

        expected.queryParameterNames.forEach { paramName ->
            expected.getQueryParameter(paramName)?.let { param ->
                params[paramName] = param
            }
        }

        return params
    }
}
