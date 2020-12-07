package hu.bme.aut.android.timetic.connectionmanager

import android.os.Handler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.network.apiorganization.OrganizationApi
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkOrganizationInteractor(private val organizationUrl: String, auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val organizationApi: OrganizationApi

    init {
        val m = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val client: OkHttpClient?
        when {
            auth != null -> {
                client =  OkHttpClient.Builder()
                    .addInterceptor(auth)
                    .build()
            }
            autb != null -> {
                client =  OkHttpClient.Builder()
                    .authenticator(TokenAuthenticator())
                    .addInterceptor(autb)
                    .build()
            }
            else -> {
                client =  OkHttpClient.Builder()
                    .build()
            }
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(organizationUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m).asLenient())
            .build()

        this.organizationApi = retrofit.create(OrganizationApi::class.java)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
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
                    if (response != null) {
                        onError(e, response.code(), call.request().toString())
                    } else {
                        onError(e, null, call.request().toString())
                    }
                }
            }
        }.start()
    }

    private fun <T> runCallOnBackgroundThreadAndAddURL(
        call: Call<T>,
        onSuccess: (T, String) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val handler = Handler()
        Thread {
            var response: Response<T>? = null
            try {
                response = call.execute()
                val body = response.body()!!
                handler.post {
                    onSuccess(body, organizationUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    if (response != null) {
                        onError(e, response.code(), call.request().toString())
                    } else {
                        onError(e, null, call.request().toString())
                    }
                }
            }
        }.start()
    }

    fun login(
        onSuccess: (ForEmployeeLoginData) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getRefreshToken = organizationApi.employeeLoginGet()
        this.runCallOnBackgroundThread(getRefreshToken, onSuccess, onError)
    }

    fun getToken(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getToken = organizationApi.employeeRefreshGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getEmployeeAppointments(
        onSuccess: (List<CommonAppointment>) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getData = organizationApi.employeeAppointmentsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getClientAppointments(
        onSuccess: (List<ForClientAppointment>, String) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getData = organizationApi.clientAppointmentsGet()
        this.runCallOnBackgroundThreadAndAddURL(getData, onSuccess, onError)
    }

    fun getDataForAppointmentCreation(
        onSuccess: (ForEmployeeDataForAppointmentCreation) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeAppointmentCreationDataGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun addAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeAppointmentsPost(appointment)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun modifyAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeAppointmentsPut(appointment)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun cancelAppointmentByEmployee(
        id: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeAppointmentsAppointmentIdDelete(id)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun cancelAppointmentByClient(
        id: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.clientAppointmentsAppointmentIdDelete(id)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getReport(
        start: Long,
        end: Long,
        onSuccess: (ForEmployeeReport) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeReportGet(start, end)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun addClient(
        client: CommonClient,
        onSuccess: (CommonClient) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeClientsPost(client)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun registerClient(
        client: CommonClient,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.clientRegisterPost(client)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun postRefreshTokenForClient(
        commonPostRefresh: CommonPostRefresh,
        onSuccess: (CommonToken, String) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.clientRefreshPost(commonPostRefresh)
        this.runCallOnBackgroundThreadAndAddURL(getData, onSuccess, onError)
    }

    fun getClients(
        onSuccess: (List<CommonClient>) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeClientsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getOrganizationDataForEmployee(
        onSuccess: (ForEmployeeOrganization) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeOrganizationGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getOrganizationDataForClient(
        email: String,
        onSuccess: (ForClientOrganization) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.clientOrganizationGet(email)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeForgottenPasswordGet(email)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun saveNewPassword(
        commonPasswordReset: CommonPasswordReset,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeForgottenPasswordPost(commonPasswordReset)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getMeetingUrl(
        appointmentId: String,
        onSuccess: (CommonConsultation) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.employeeConsultationGet(appointmentId)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getMeetingUrlForClient(
        appointmentId: String,
        onSuccess: (CommonConsultation) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organizationApi.clientConsultationGet(appointmentId)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }
}