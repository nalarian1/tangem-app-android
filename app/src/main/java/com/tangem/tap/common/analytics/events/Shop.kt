package com.tangem.tap.common.analytics.events

/**
 * Created by Anton Zhilenkov on 28.09.2022.
 */
sealed class Shop(
    event: String,
    params: Map<String, String> = mapOf(),
) : AnalyticsEvent("Shop", event, params) {

    class ScreenOpened : IntroductionProcess("Shop Screen Opened")

    class Purchased(productSku: String) : Shop(
        event = "Purchased",
        params = mapOf("SKU" to productSku),
    )

    class Redirected(partnerName: String) : Shop(
        event = "Redirected",
        params = mapOf("Partner" to partnerName),
    )
}