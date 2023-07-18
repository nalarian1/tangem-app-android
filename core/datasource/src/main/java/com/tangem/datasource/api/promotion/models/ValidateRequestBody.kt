package com.tangem.datasource.api.promotion.models

import com.squareup.moshi.Json

/**
 * @author Anton Zhilenkov on 05.06.2023.
 */
data class ValidateRequestBody(
    @Json(name = "walletId") val walletId: String,
    @Json(name = "programName") val programName: String,
)