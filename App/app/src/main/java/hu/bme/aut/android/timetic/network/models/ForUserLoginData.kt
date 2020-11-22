package hu.bme.aut.android.timetic.network.models

import com.squareup.moshi.Json

/**
 *
 * @param user
 * @param refreshToken
 */

data class ForUserLoginData (
    @Json(name = "user")
    val user: CommonUser? = null,
    @Json(name = "refreshToken")
    val refreshToken: CommonToken? = null
)

