package hu.bme.aut.android.timetic.dataManager

import android.content.Intent
import android.os.Handler
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.network.apiOrganisation.EmployeeApi
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.infrastructure.Serializer
import hu.bme.aut.android.timetic.network.models.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkOrganisationInteractor(organisationUrl: String, auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val employeeApi: EmployeeApi

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
                .addInterceptor(AuthorizationInterceptor())
                .build()
        }
        else{
            client =  OkHttpClient.Builder()
                .addInterceptor(AuthorizationInterceptor())
                .build()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(organisationUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m).asLenient())
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
                Log.d("EZAZ", "response before")
                val response = call.execute().body()!!
                Log.d("EZAZ", "response after")
                handler.post {
                    onSuccess(response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    //logerror
                    onError(e)
                }
            }
        }.start()
    }

    fun logoutUser(){
        MyApplication.appContext.sendBroadcast(Intent("Logout"))
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
        val getData = employeeApi.employeeAppointmentsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getDataForAppointmentCreation(
        onSuccess: (ForEmployeeDataForAppointmentCreation) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeAppointmentCreationDataGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun addAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeAppointmentsPost(appointment)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun modifyAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeAppointmentsPut(appointment)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun cancelAppointment(
        id: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeAppointmentsAppointmentIdDelete(id)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getReport(
        start: Long,
        end: Long,
        onSuccess: (ForEmployeeReport) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeReportGet(start, end)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getReportClients(
        onSuccess: (List<CommonClient>) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeClientsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun addClient(
        client: CommonClient,
        onSuccess: (CommonClient) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeClientsPost(client)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getClients(
        onSuccess: (List<CommonClient>) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeClientsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getOrganisationData(
        onSuccess: (ForEmployeeOrganization) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeOrganizationGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeForgottenPassword(email)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun saveNewPassword(
        commonPasswordReset: CommonPasswordReset,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeForgottenPassword(commonPasswordReset)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getMeetingUrl(
        appointmentId: String,
        onSuccess: (CommonConsultation) -> Unit,
        onError: (Throwable) -> Unit
    ){
        val getData = employeeApi.employeeConsultationGet(appointmentId)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }
}