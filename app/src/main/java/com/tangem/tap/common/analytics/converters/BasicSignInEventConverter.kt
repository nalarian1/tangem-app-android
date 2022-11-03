package com.tangem.tap.common.analytics.converters

import com.tangem.common.Converter
import com.tangem.common.extensions.isZero
import com.tangem.domain.common.ScanResponse
import com.tangem.tap.common.analytics.events.AnalyticsParam
import com.tangem.tap.common.analytics.events.Basic
import com.tangem.tap.domain.extensions.getUserWalletId
import com.tangem.tap.features.wallet.redux.ProgressState
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 02.11.2022.
 */
class BasicSignInEventConverter(
    private val scanResponse: ScanResponse,
    private val progressState: ProgressState,
    private val totalCryptoAmount: BigDecimal,
) : Converter<Any?, Basic.SignedIn?> {

    override fun convert(value: Any?): Basic.SignedIn? {
        if (progressState == ProgressState.Loading || progressState == ProgressState.Refreshing) return null
        val cardCurrency = ParamCardCurrencyConverter().convert(scanResponse) ?: return null

        val cardState = when {
            totalCryptoAmount.isZero() -> AnalyticsParam.CardState.Empty
            else -> AnalyticsParam.CardState.Full
        }

        return Basic.SignedIn(
            state = cardState,
            currency = cardCurrency,
            batch = scanResponse.card.batchId,
        ).apply {
            filterData = scanResponse.card.getUserWalletId()
        }
    }
}
