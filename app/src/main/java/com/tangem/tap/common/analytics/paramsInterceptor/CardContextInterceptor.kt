package com.tangem.tap.common.analytics.paramsInterceptor

import com.tangem.core.analytics.api.ParamsInterceptor
import com.tangem.core.analytics.models.AnalyticsEvent
import com.tangem.domain.common.util.cardTypesResolver
import com.tangem.domain.models.scan.ProductType
import com.tangem.domain.models.scan.ScanResponse
import com.tangem.domain.wallets.builder.UserWalletIdBuilder
import com.tangem.tap.common.analytics.converters.ParamCardCurrencyConverter
import com.tangem.tap.common.analytics.events.AnalyticsParam
import com.tangem.tap.common.analytics.events.IntroductionProcess
import com.tangem.tap.common.analytics.events.MainScreen
import com.tangem.tap.features.demo.DemoHelper

/**
 * Created by Anton Zhilenkov on 17.02.2023.
 */
class CardContextInterceptor(
    private val scanResponse: ScanResponse,
) : ParamsInterceptor {

    private val userWalletId = UserWalletIdBuilder.scanResponse(scanResponse).build()

    override fun id(): String = CardContextInterceptor.id()

    override fun canBeAppliedTo(event: AnalyticsEvent): Boolean {
        return when (event) {
            is IntroductionProcess.ButtonScanCard, is MainScreen.ButtonScanCard -> false
            else -> true
        }
    }

    override fun intercept(params: MutableMap<String, String>) {
        val card = scanResponse.card
        params[AnalyticsParam.BATCH] = card.batchId
        params[AnalyticsParam.PRODUCT_TYPE] = getProductType(scanResponse)
        params[AnalyticsParam.FIRMWARE] = card.firmwareVersion.stringValue
        if (userWalletId != null) {
            params[AnalyticsParam.USER_WALLET_ID] = userWalletId.stringValue
        }

        ParamCardCurrencyConverter().convert(scanResponse.cardTypesResolver)?.let {
            params[AnalyticsParam.CURRENCY] = it.value
        }
    }

    private fun getProductType(scanResponse: ScanResponse): String {
        return when (scanResponse.productType) {
            ProductType.Note -> "Note"
            ProductType.Twins -> "Twin"
            ProductType.Wallet -> "Wallet"
            ProductType.Wallet2 -> "Wallet 2.0"
            ProductType.Ring -> "Ring"
            ProductType.Start2Coin -> "Start2Coin"
            ProductType.Visa -> "VISA"
            else -> if (DemoHelper.isDemoCard(scanResponse)) {
                if (DemoHelper.isTestDemoCard(scanResponse)) {
                    "Demo Test"
                } else {
                    when (scanResponse.card.cardId.substring(0..1)) {
                        "AC" -> "Demo Wallet"
                        "AB" -> "Demo Note"
                        else -> "Demo Other"
                    }
                }
            } else {
                "Other"
            }
        }
    }

    companion object {
        fun id(): String = CardContextInterceptor::class.java.simpleName
    }
}
