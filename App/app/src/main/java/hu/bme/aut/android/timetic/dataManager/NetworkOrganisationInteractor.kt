package hu.bme.aut.android.timetic.dataManager

import android.os.Handler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.network.apiOrganisation.OrganisationApi
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkOrganisationInteractor(private val organisationUrl: String, auth: HttpBasicAuth?, autb: HttpBearerAuth?) {
    private val organisationApi: OrganisationApi

    init {
        val m = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        var client: OkHttpClient? = null
        when {
            auth != null -> {
                client =  OkHttpClient.Builder()
                    .addInterceptor(auth)
                    .build()
            }
            autb != null -> {
                client =  OkHttpClient.Builder()
                    .addInterceptor(autb)
                    .addInterceptor(AuthorizationInterceptor())
                    .build()
            }
            else -> {
                client =  OkHttpClient.Builder()
                    .addInterceptor(AuthorizationInterceptor())
                    .build()
            }
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(organisationUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m).asLenient())
            .build()

        this.organisationApi = retrofit.create(OrganisationApi::class.java)
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
                    onSuccess(body, organisationUrl)
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
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getRefreshToken = organisationApi.employeeLoginGet()
        this.runCallOnBackgroundThread(getRefreshToken, onSuccess, onError)
    }

    fun getToken(
        onSuccess: (CommonToken) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getToken = organisationApi.employeeRefreshGet()
        this.runCallOnBackgroundThread(getToken, onSuccess, onError)
    }

    fun getTokenForClient(
        onSuccess: (CommonToken, String) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getToken = organisationApi.clientRefreshGet()
        this.runCallOnBackgroundThreadAndAddURL(getToken, onSuccess, onError)
    }

    fun getEmployeeAppointments(
        onSuccess: (List<CommonAppointment>) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getData = organisationApi.employeeAppointmentsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getClientAppointments(
        onSuccess: (List<ForClientAppointment>, String) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ) {
        val getData = organisationApi.clientAppointmentsGet()
        this.runCallOnBackgroundThreadAndAddURL(getData, onSuccess, onError)
    }

    fun getDataForAppointmentCreation(
        onSuccess: (ForEmployeeDataForAppointmentCreation) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeAppointmentCreationDataGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun addAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeAppointmentsPost(appointment)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun modifyAppointment(
        appointment: CommonAppointment,
        onSuccess: (CommonAppointment) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeAppointmentsPut(appointment)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun cancelAppointmentByEmployee(
        id: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeAppointmentsAppointmentIdDelete(id)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun cancelAppointmentByClient(
        id: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.clientAppointmentsAppointmentIdDelete(id)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getReport(
        start: Long,
        end: Long,
        onSuccess: (ForEmployeeReport) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeReportGet(start, end)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun addClient(
        client: CommonClient,
        onSuccess: (CommonClient) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeClientsPost(client)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun registerClient(
        client: CommonClient,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.clientRegisterPost(client)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun postRefreshTokenForClient(
        commonPostRefresh: CommonPostRefresh,
        onSuccess: (CommonToken, String) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.clientRefreshPost(commonPostRefresh)
        this.runCallOnBackgroundThreadAndAddURL(getData, onSuccess, onError)
    }

    fun getClients(
        onSuccess: (List<CommonClient>) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeClientsGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getOrganisationDataForEmployee(
        onSuccess: (ForEmployeeOrganization) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeOrganizationGet()
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getOrganisationDataForClient(
        email: String,
        onSuccess: (ForClientOrganization) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.clientOrganizationGet(email)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeForgottenPasswordGet(email)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun saveNewPassword(
        commonPasswordReset: CommonPasswordReset,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeForgottenPasswordPost(commonPasswordReset)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getMeetingUrl(
        appointmentId: String,
        onSuccess: (CommonConsultation) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.employeeConsultationGet(appointmentId)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }

    fun getMeetingUrlForClient(
        appointmentId: String,
        onSuccess: (CommonConsultation) -> Unit,
        onError: (Throwable, Int?, String) -> Unit
    ){
        val getData = organisationApi.clientConsultationGet(appointmentId)
        this.runCallOnBackgroundThread(getData, onSuccess, onError)
    }
}