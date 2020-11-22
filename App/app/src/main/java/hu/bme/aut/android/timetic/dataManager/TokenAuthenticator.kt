package hu.bme.aut.android.timetic.dataManager

import android.content.Intent
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.network.models.CommonToken
import okhttp3.*

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
        val apiOrg = MyApplication.getOrganizationApiForRefresh()
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
}