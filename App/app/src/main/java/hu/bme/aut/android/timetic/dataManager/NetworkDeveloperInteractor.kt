package hu.bme.aut.android.timetic.dataManager

import android.os.Handler
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.apiDeveloper.DeveloperApi
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.network.models.CommonPasswordReset
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.network.models.ForMobileUserRegistration
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkDeveloperInteractor(auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val developerApi: DeveloperApi

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
            .baseUrl("https://optipus.ddns.net:8080")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m))
            .build()

        this.developerApi = retrofit.create(DeveloperApi::class.java)
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
                response = call.execute()
                val body = response.body()!!
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
        val getImagesRequest = developerApi.mobileOrganizationsGet()
        this.runCallOnBackgroundThread(getImagesRequest, onSuccess, onError)
    }

    fun registerUser(user: ForMobileUserRegistration,
                     onSuccess: (Unit) -> Unit,
                     onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getImagesRequest = developerApi.mobileRegisterPost(user)
        this.runCallOnBackgroundThread(getImagesRequest, onSuccess, onError)
    }

    fun login(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getRefreshToken = developerApi.mobileLoginGet()
        this.runCallOnBackgroundThread(getRefreshToken, onSuccess, onError)
    }

    fun getToken(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getToken = developerApi.mobileRefreshGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getRegisteredOrganisations(
        onSuccess: (List<CommonOrganization>) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getToken = developerApi.mobileRegisteredOrganizationsGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun patchRegisteredOrganisationById(
        organisationId: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, code: Int?, call: String) -> Unit
    ) {
        val getToken = developerApi.mobileRegisteredOrganizationsIdPatch(organisationId)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = developerApi.mobileForgottenPassword(email)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun saveNewPassword(
        commonPasswordReset: CommonPasswordReset,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = developerApi.mobileForgottenPassword(commonPasswordReset)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }
}