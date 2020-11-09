package hu.bme.aut.android.timetic.network.models

import com.squareup.moshi.Json

/**
 *
 * @param url
 */
data class CommonConsultation (
    @Json(name = "url")
    val url: kotlin.String? = null
)