package hu.bme.aut.android.timetic.network.apiorganization

import hu.bme.aut.android.timetic.network.models.*
import retrofit2.http.*
import retrofit2.Call

interface OrganizationApi {
    /**
     * Cancel appointment
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: resource not found
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param appointmentId  
    * @return [Call]<[Unit]>
     */
    @DELETE("client/appointments/{appointmentId}")
    fun clientAppointmentsAppointmentIdDelete(@Path("appointmentId") appointmentId: kotlin.String): Call<Unit>

    /**
     * Lists the appointments
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[kotlin.collections.List<ForClientAppointment>]>
     */
    @GET("client/appointments")
    fun clientAppointmentsGet(): Call<kotlin.collections.List<ForClientAppointment>>

    /**
     * Lists details of the organization // no authentication required
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[ForClientOrganization]>
     */
    @GET("client/organization")
    fun clientOrganizationGet(@Query("email") email: String): Call<ForClientOrganization>

    /**
     * Sets client's refresh token
     *
     * Responses:
     *  - 200: successful operation
     *
     * @return [Call]<[CommonToken]>
     */
    @POST("client/refresh")
    fun clientRefreshPost(@Body commonPostRefresh: CommonPostRefresh): Call<CommonToken>

    /**
     * Refresh client&#39;s token
     * 
     * Responses:
     *  - 200: successful operation
     *  - 400: Invalid username/password supplied
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[CommonToken]>
     */
    @GET("client/refresh")
    fun clientRefreshGet(): Call<CommonToken>

    /**
     * Registers client // needs to fill the form with personal info
     * 
     * Responses:
     *  - 201: successfully created
     *  - 409: already exists
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param commonClient  
    * @return [Call]<[CommonClient]>
     */
    @POST("client/register")
    fun clientRegisterPost(@Body commonClient: CommonClient): Call<Unit>

    /**
     * Gets the url of the online consultation for the appointment if that is online
     *
     * Responses:
     *  - 200: successful operation
     *  - 404: resource not found
     *  - 401: Not authenticated
     *
     * @param appointmentId
     * @return [Call]<[kotlin.String]>
     */
    @GET("client/consultation")
    fun clientConsultationGet(@Query("appointmentId") appointmentId: kotlin.String): Call<CommonConsultation>

     /**
     * Gets the necessary data for create an appointment
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[ForEmployeeDataForAppointmentCreation]>
     */
    @GET("employee/appointmentCreationData")
    fun employeeAppointmentCreationDataGet(): Call<ForEmployeeDataForAppointmentCreation>

    /**
     * Cancels appointment
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param appointmentId  
    * @return [Call]<[Unit]>
     */
    @DELETE("employee/appointments/{appointmentId}")
    fun employeeAppointmentsAppointmentIdDelete(@Path("appointmentId") appointmentId: kotlin.String): Call<Unit>

    /**
     * Gets the appointment with the given id
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: resource not found
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param appointmentId  
    * @return [Call]<[CommonAppointment]>
     */
    @GET("employee/appointments/{appointmentId}")
    fun employeeAppointmentsAppointmentIdGet(@Path("appointmentId") appointmentId: kotlin.String): Call<CommonAppointment>

    /**
     * Lists appointments for the employee
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param startDate  (optional)
     * @param endDate  (optional)
     * @param clientName  (optional)
    * @return [Call]<[kotlin.collections.List<CommonAppointment>]>
     */
    @GET("employee/appointments")
    fun employeeAppointmentsGet(@Query("startDate") startDate: kotlin.Long? = null, @Query("endDate") endDate: kotlin.Long? = null, @Query("clientName") clientName: kotlin.String? = null): Call<kotlin.collections.List<CommonAppointment>>

    /**
     * Uploads a new appointment
     * 
     * Responses:
     *  - 201: successfully created
     *  - 409: already exists
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param commonAppointment  
    * @return [Call]<[CommonAppointment]>
     */
    @POST("employee/appointments")
    fun employeeAppointmentsPost(@Body commonAppointment: CommonAppointment): Call<CommonAppointment>

    /**
     * Modifies appointment
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: Invalid parameters or ID not found
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param commonAppointment  
    * @return [Call]<[CommonAppointment]>
     */
    @PUT("employee/appointments")
    fun employeeAppointmentsPut(@Body commonAppointment: CommonAppointment): Call<CommonAppointment>

    /**
     * Lists the clients who are registered to the organization
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: resource not found
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param name  (optional)
    * @return [Call]<[kotlin.collections.List<CommonClient>]>
     */
    @GET("employee/clients")
    fun employeeClientsGet(@Query("name") name: kotlin.String? = null): Call<kotlin.collections.List<CommonClient>>

    /**
     * Registers a client
     * 
     * Responses:
     *  - 201: successfully created
     *  - 409: already exists
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param commonClient  
    * @return [Call]<[CommonClient]>
     */
    @POST("employee/clients")
    fun employeeClientsPost(@Body commonClient: CommonClient): Call<CommonClient>

    /**
     * Asks for new password
     *
     * Responses:
     *  - 200: successful operation
     *  - 404: account not found
     *
     * @return [Call]<[Unit]>
     */
    @GET("employee/forgottenPassword/")
    fun employeeForgottenPasswordGet(@Query("email") email: kotlin.String): Call<Unit>

    /**
     * Create new password
     *
     * Responses:
     *  - 200: successful operation
     *  - 404: account not found
     *
     * @return [Call]<[Unit]>
     */
    @POST("employee/forgottenPassword")
    fun employeeForgottenPasswordPost(@Body commonPasswordReset: CommonPasswordReset): Call<Unit>

    /**
     * Logs employee into the system
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: Invalid username/password supplied
     *  - 401: Not authenticated
     * 
    * @return [Call]<[CommonToken]>
     */
    @GET("employee/login")
    fun employeeLoginGet(): Call<ForEmployeeLoginData>

    /**
     * Logs employee out of the system
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[Unit]>
     */
    @GET("employee/logout")
    fun employeeLogoutGet(): Call<Unit>

    /**
     * Returns the organization of the employee
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[ForEmployeeOrganization]>
     */
    @GET("employee/organization")
    fun employeeOrganizationGet(): Call<ForEmployeeOrganization>

    /**
     * Refresh employee&#39;s token
     * 
     * Responses:
     *  - 200: successful operation
     *  - 400: Invalid username/password supplied
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[CommonToken]>
     */
    @GET("employee/refresh")
    fun employeeRefreshGet(): Call<CommonToken>

    /**
     * Creates a report of the work of the given period
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param startDate  (optional)
     * @param endDate  (optional)
    * @return [Call]<[ForEmployeeReport]>
     */
    @GET("employee/report")
    fun employeeReportGet(@Query("startDate") startDate: kotlin.Long? = null, @Query("endDate") endDate: kotlin.Long? = null): Call<ForEmployeeReport>

    /**
     * Gets the url of the online consultation for the appointment if that is online
     *
     * Responses:
     *  - 200: successful operation
     *  - 404: resource not found
     *  - 401: Not authenticated
     *
     * @param appointmentId
     * @return [Call]<[kotlin.String]>
     */
    @GET("employee/consultation")
    fun employeeConsultationGet(@Query("appointmentId") appointmentId: kotlin.String): Call<CommonConsultation>
}
