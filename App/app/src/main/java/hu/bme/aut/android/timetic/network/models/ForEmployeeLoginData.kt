package hu.bme.aut.android.timetic.network.models

import com.squareup.moshi.Json

/**
 *
 * @param user
 * @param refreshToken
 */

data class ForEmployeeLoginData (
    @Json(name = "employee")
    val employee: CommonEmployee? = null,
    @Json(name = "refreshToken")
    val refreshToken: CommonToken? = null
)
