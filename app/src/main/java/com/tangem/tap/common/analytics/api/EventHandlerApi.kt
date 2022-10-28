package com.tangem.tap.common.analytics.api

import com.shopify.buy3.Storefront
import com.tangem.blockchain.common.BlockchainError
import com.tangem.common.card.Card
import com.tangem.common.core.TangemSdkError
import com.tangem.tap.common.analytics.AnalyticsAnOld
import com.tangem.tap.common.analytics.AnalyticsEventAnOld
import com.tangem.tap.common.analytics.AnalyticsParamAnOld
import com.tangem.tap.common.analytics.events.AnalyticsEvent
import com.tangem.tap.common.analytics.events.AnalyticsParam
import com.tangem.tap.common.extensions.filterNotNull

/**
 * Created by Anton Zhilenkov on 23/09/2022.
 */
interface AnalyticsEventHandler {
    fun handleEvent(
        event: String,
        params: Map<String, String> = emptyMap(),
    )

    fun handleAnalyticsEvent(
        event: AnalyticsEvent,
        card: Card? = null,
        blockchain: String? = null,
    ) {
        handleEvent(
            event = prepareEventString(event.category, event.event),
            params = prepareParams(card, blockchain, event.params),
        )
    }

    @Deprecated("Migrate to AnalyticsEvent")
    fun handleAnalyticsEvent(
        event: AnalyticsEventAnOld,
        params: Map<String, String> = emptyMap(),
        card: Card? = null,
        blockchain: String? = null,
    ) {
        handleEvent(event.event, prepareParams(card, blockchain, params))
    }

    fun prepareParams(
        card: Card? = null,
        blockchain: String? = null,
        params: Map<String, String> = emptyMap(),
    ): Map<String, String> = mapOf(
        AnalyticsParam.Firmware to card?.firmwareVersion?.stringValue,
        AnalyticsParam.BatchId to card?.batchId,
        AnalyticsParam.Blockchain to blockchain,
    ).filterNotNull() + params

    fun prepareEventString(category: String, event: String): String {
        return "[$category] $event"
    }
}

interface ErrorEventHandler {
    fun handleErrorEvent(
        error: Throwable,
        params: Map<String, String> = emptyMap(),
    )
}

interface SdkErrorEventHandler : CardSdkErrorEventHandler, BlockchainSdkErrorEventHandler

interface CardSdkErrorEventHandler {
    fun handleCardSdkErrorEvent(
        error: TangemSdkError,
        action: AnalyticsAnOld.ActionToLog,
        params: Map<AnalyticsParamAnOld, String> = emptyMap(),
        card: Card? = null,
    )
}

interface BlockchainSdkErrorEventHandler {
    fun handleBlockchainSdkErrorEvent(
        error: BlockchainError,
        action: AnalyticsAnOld.ActionToLog,
        params: Map<AnalyticsParamAnOld, String> = mapOf(),
        card: Card? = null,
    )
}

interface ShopifyOrderEventHandler {
    fun handleShopifyOrderEvent(order: Storefront.Order)
}

