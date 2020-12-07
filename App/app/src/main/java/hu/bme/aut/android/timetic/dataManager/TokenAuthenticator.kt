package hu.bme.aut.android.timetic.dataManager

import android.content.Intent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.network.apiOrganization.OrganizationApi
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonToken
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val updatedToken = getUpdatedToken()
        var requestAvailable: Request? = null
        try {
            requestAvailable = response.request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $updatedToken")
                .build()
            return requestAvailable
        } catch (ex: Exception) { }
        return requestAvailable
    }

    private fun getUpdatedToken(): String {
        // request to login API to get fresh token
        // synchronously calling login API
        val apiOrg = getOrganizationApiForRefresh()
        val loginResponse: retrofit2.Response<CommonToken> = apiOrg.employeeRefreshGet().execute()
        var newToken = ""
        if (loginResponse.isSuccessful) {
            newToken = loginResponse.body()!!.token!!

            val secureSharedPreferences = MyApplication.secureSharedPreferences
            val editor = secureSharedPreferences.edit()
            editor.putString("Token", newToken)
            editor.apply()
        }
        else{
            MyApplication.appContext.sendBroadcast(Intent("Logout"))
        }
        return newToken
    }

    fun getOrganizationApiForRefresh(): OrganizationApi {
        val m = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val client =  OkHttpClient.Builder()
            .addInterceptor(
                HttpBearerAuth(
                "bearer",
                MyApplication.getRefreshToken()!!
            )
            )
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(MyApplication.secureSharedPreferences.getString("OrganizationUrl", "").toString())
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m))
            .build()

        return retrofit.create(OrganizationApi::class.java)
    }
}