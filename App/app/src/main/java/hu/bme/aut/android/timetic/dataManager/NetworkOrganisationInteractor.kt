package hu.bme.aut.android.timetic.dataManager

import android.os.Handler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.network.apiOrganisation.EmployeeApi
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.infrastructure.Serializer
import hu.bme.aut.android.timetic.network.models.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkOrganisationInteractor(organisationUrl: String, auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val employeeApi: EmployeeApi
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
            .baseUrl(organisationUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m))
            .build()

        this.employeeApi = retrofit.create(EmployeeApi::class.java)
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
                handler.post {
                    onSuccess(response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    onError(e)
                }
            }
        }.start()
    }

    fun login(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getRefreshToken = employeeApi.employeeLoginGet()
        this.runCallOnBackgroundThread(getRefreshToken, onSuccess, onError)
    }

    fun getToken(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getToken = employeeApi.employeeRefreshGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getAppointments(
        onSuccess: (List<CommonAppointment>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getToken = employeeApi.employeeAppointmentsGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getDataForAppointmentCreation(
        onSuccess: (ForEmployeeDataForAppointmentCreation) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeAppointmentCreationDataGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun addAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeAppointmentsPost(appointment)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun modifyAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeAppointmentsPut(appointment)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun cancelAppointment(
        id: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeAppointmentsAppointmentIdDelete(id)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getReport(
        start: Long,
        end: Long,
        onSuccess: (ForEmployeeReport) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeReportGet(start, end)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getReportClients(
        onSuccess: (List<CommonClient>) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeClientsGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun addClient(
        client: CommonClient,
        onSuccess: (CommonClient) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeClientsPost(client)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getClients(
        onSuccess: (List<CommonClient>) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeClientsGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getAppointment(
        id: String,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getToken = employeeApi.employeeAppointmentsAppointmentIdGet(id)
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

}