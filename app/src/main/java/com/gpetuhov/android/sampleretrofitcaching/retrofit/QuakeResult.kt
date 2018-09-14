package com.gpetuhov.android.samplemoshi.retrofit

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
class QuakeResult(
    @Json(name = "features") val quakeList: List<QuakeModel>
)