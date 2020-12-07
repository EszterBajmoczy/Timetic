package hu.bme.aut.android.timetic

/**
 * Authentication result : success (user details) or error message.
 */
data class Result(
        val success: Boolean? = null,
        val error: Int? = null
)
