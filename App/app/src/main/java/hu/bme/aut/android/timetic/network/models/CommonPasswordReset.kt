package hu.bme.aut.android.timetic.network.models

import com.squareup.moshi.Json

/**
 *
 * @param email
 * @param newPassword
 * @param code
 */
data class CommonPasswordReset(
    @Json(name = "email")
    val email: kotlin.String? = null,
    @Json(name = "newPassword")
    val newPassword: kotlin.String? = null,
    @Json(name = "code")
    val code: kotlin.Int? = null
)