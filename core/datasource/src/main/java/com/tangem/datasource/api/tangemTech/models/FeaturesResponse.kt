package com.tangem.datasource.api.tangemTech.models

import com.squareup.moshi.Json

data class FeaturesResponse(
    @Json(name = "send") val isNewSendEnabled: Boolean,
)
