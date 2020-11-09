package hu.bme.aut.android.timetic.dataManager

import android.os.Handler
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.infrastructure.Serializer
import hu.bme.aut.android.timetic.network.apiDeveloper.MobileApi
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.network.models.ForMobileUserRegistration
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkDeveloperInteractor(auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val mobileApi: MobileApi

    init {
        val m = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        var client: OkHttpClient? = null
        if (auth != null) {
            client =  OkHttpClient.Builder()
                .addInterceptor(auth)
                .build()
        }
        else if(autb != null){
            client =  OkHttpClient.Builder()
                .addInterceptor(autb)
                .build()

        }
        else{
            client =  OkHttpClient.Builder()
                .build()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://optipus.ddns.net:8080")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m))
            .build()

        this.mobileApi = retrofit.create(MobileApi::class.java)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val handler = Handler()
        Thread {
            var response: Response<T>? = null
            try {
                Log.d("EZAZ", "response before")
                response = call.execute()
                val body = response.body()!!
                Log.d("EZAZ", "response after")
                handler.post {
                    onSuccess(body)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    //logerror
                    if (response != null) {
                        onError(e, response.code(), call.request().toString())
                    } else {
                        onError(e, null, call.request().toString())
                    }
                }
            }
        }.start()
    }

    fun getOrganisations(
        onSuccess: (List<CommonOrganization>) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getImagesRequest = mobileApi.mobileOrganizationsGet()
        this.runCallOnBackgroundThread(getImagesRequest, onSuccess, onError)
    }

    fun registerUser(user: ForMobileUserRegistration,
                     onSuccess: (Unit) -> Unit,
                     onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getImagesRequest = mobileApi.mobileRegisterPost(user)
        this.runCallOnBackgroundThread(getImagesRequest, onSuccess, onError)
    }

    fun login(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getRefreshToken = mobileApi.mobileLoginGet()
        this.runCallOnBackgroundThread(getRefreshToken, onSuccess, onError)
    }

    fun getToken(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getToken = mobileApi.mobileRefreshGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }
}