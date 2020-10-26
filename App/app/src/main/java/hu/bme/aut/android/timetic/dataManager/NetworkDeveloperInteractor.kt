package hu.bme.aut.android.timetic.dataManager

import android.os.Handler
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
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkDeveloperInteractor(auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val mobileApi: MobileApi
    private val serializerBuilder: Moshi.Builder = Serializer.moshiBuilder

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
        onError: (Throwable) -> Unit
    ) {
        val handler = Handler()
        Thread {
            try {
                val response = call.execute().body()!!
                handler.post { onSuccess(response) }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { onError(e) }
            }
        }.start()
    }

    fun getOrganisations(
        onSuccess: (List<CommonOrganization>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getImagesRequest = mobileApi.mobileOrganizationsGet()
        this.runCallOnBackgroundThread(getImagesRequest, onSuccess, onError)
    }

    fun registerUser(user: ForMobileUserRegistration,
                     onSuccess: (Unit) -> Unit,
                     onError: (Throwable) -> Unit
    ) {
        val getImagesRequest = mobileApi.mobileRegisterPost(user)
        this.runCallOnBackgroundThread(getImagesRequest, onSuccess, onError)
    }

    fun login(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getRefreshToken = mobileApi.mobileLoginGet()
        this.runCallOnBackgroundThread(getRefreshToken, onSuccess, onError)
    }

    fun getToken(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getToken = mobileApi.mobileRefreshGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }
}