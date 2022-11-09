package com.tangem.tap.common.analytics.events

/**
 * Created by Anton Zhilenkov on 28.09.2022.
 */
sealed class Chat(
    event: String,
    params: Map<String, String> = mapOf(),
) : AnalyticsEvent("Chat", event, params) {

    class ScreenOpened : Chat("Chat Screen Opened")
}