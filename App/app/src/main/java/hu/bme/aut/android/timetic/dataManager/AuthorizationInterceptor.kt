package hu.bme.aut.android.timetic.dataManager

import android.content.Intent
import android.util.Log
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.network.models.CommonToken
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthorizationInterceptor() : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var mainResponse: Response = chain.proceed(chain.request())
        val mainRequest: Request = chain.request()
        if (mainResponse.code == 401 || mainResponse.code == 403) {
            // request to login API to get fresh token
            // synchronously calling login API
            val apiOrg = MyApplication.getOrganisationApiForRefresh()
            val loginResponse: retrofit2.Response<CommonToken> = apiOrg.employeeRefreshGet().execute()

            Log.d("EZAZ", "1")

            if (loginResponse.isSuccessful) {
                val secureSharedPreferences = MyApplication.secureSharedPreferences

                val editor = secureSharedPreferences.edit()
                editor.putString("Token", loginResponse.body()!!.token)
                editor.apply()

                // retry the 'mainRequest' which encountered an authentication error
                // add new token into 'mainRequest' header and request again
                val builder: Request.Builder =
                    mainRequest.newBuilder()
                        .header("Authorization", "Bearer "+loginResponse.body()!!.token!!)
                        .method(mainRequest.method, mainRequest.body)
                mainResponse.close()
                mainResponse = chain.proceed(builder.build())
            }
            else{
                MyApplication.appContext.sendBroadcast(Intent("Logout"))
            }
        }
        return mainResponse
    }
}