package hu.bme.aut.android.timetic.network.apis

import retrofit2.http.*
import retrofit2.Call
import hu.bme.aut.android.timetic.network.models.*

interface AdminApi {
    /**
     * Lists the activities that are connected to the employee
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[kotlin.collections.List<CommonActivity>]>
     */
    @GET("admin/activities")
    fun adminActivitiesGet(): Call<kotlin.collections.List<CommonActivity>>

    /**
     * Cancels appointment
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
    @DELETE("admin/appointments/{appointmentId}")
    fun adminAppointmentsAppointmentIdDelete(@Path("appointmentId") appointmentId: kotlin.String): Call<Unit>

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
    @GET("admin/appointments/{appointmentId}")
    fun adminAppointmentsAppointmentIdGet(@Path("appointmentId") appointmentId: kotlin.String): Call<CommonAppointment>

    /**
     * Lists appointments for the employee
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[kotlin.collections.List<CommonAppointment>]>
     */
    @GET("admin/appointments")
    fun adminAppointmentsGet(): Call<kotlin.collections.List<CommonAppointment>>

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
    @POST("admin/appointments")
    fun adminAppointmentsPost(@Body commonAppointment: CommonAppointment): Call<CommonAppointment>

    /**
     * Modifies appointment
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
     * @param commonAppointment  
    * @return [Call]<[CommonAppointment]>
     */
    @PUT("admin/appointments")
    fun adminAppointmentsPut(@Body commonAppointment: CommonAppointment): Call<CommonAppointment>

    /**
     * Lists the clients who are registered to the organization
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[kotlin.collections.List<CommonClient>]>
     */
    @GET("admin/clients")
    fun adminClientsGet(): Call<kotlin.collections.List<CommonClient>>

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
    @POST("admin/clients")
    fun adminClientsPost(@Body commonClient: CommonClient): Call<CommonClient>

    /**
     * Asks for new password
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: account not found
     * 
    * @return [Call]<[Unit]>
     */
    @GET("admin/forgottenPassword")
    fun adminForgottenPasswordGet(): Call<Unit>

    /**
     * Logs employee into the system
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Invalid username/password supplied
     * 
     * @param forAdminLogin  
    * @return [Call]<[Unit]>
     */
    @POST("admin/login")
    fun adminLoginPost(@Body forAdminLogin: ForAdminLogin): Call<Unit>

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
    @GET("admin/logout")
    fun adminLogoutGet(): Call<Unit>

    /**
     * Loads data for organization details page
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[ForAdminOrganization]>
     */
    @GET("admin/organization")
    fun adminOrganizationGet(): Call<ForAdminOrganization>

    /**
     * Loads data for overview page
     * 
     * Responses:
     *  - 200: successful operation
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[ForAdminOverview]>
     */
    @GET("admin/overview")
    fun adminOverviewGet(): Call<ForAdminOverview>

    /**
     * Creates a report of the work of the given period
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: resource not found
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[ForAdminReport]>
     */
    @GET("admin/report")
    fun adminReportGet(): Call<ForAdminReport>

    /**
     * Loads admin page
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[Unit]>
     */
    @GET("")
    fun rootGet(): Call<Unit>

}
