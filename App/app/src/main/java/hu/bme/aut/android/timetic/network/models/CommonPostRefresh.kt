package hu.bme.aut.android.timetic.network.models

import com.squareup.moshi.Json

/**
 *
 * @param email
 * @param refreshToken
 */
data class CommonPostRefresh(
    @Json(name = "email")
    val email: kotlin.String? = null,
    @Json(name = "refreshToken")
    val newPassword: kotlin.String? = null
)