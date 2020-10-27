package hu.bme.aut.android.timetic.dataManager

import hu.bme.aut.android.timetic.network.models.CommonToken
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiRefreshToken {
    companion object {
        private const val REFRESH_TOKEN = "/refreshToken"
    }

    @FormUrlEncoded
    @POST(REFRESH_TOKEN)
    fun refreshToken(@Field("refreshToken") refreshToken: String?): Call<CommonToken>
}