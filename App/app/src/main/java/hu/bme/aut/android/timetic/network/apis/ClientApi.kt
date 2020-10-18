package hu.bme.aut.android.timetic.network.apis

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.network.models.ForClientAppointment
import hu.bme.aut.android.timetic.network.models.ForClientOrganization

interface ClientApi {
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
     * Logs client into the system
     * 
     * Responses:
     *  - 200: successful operation
     *  - 400: Invalid username/password supplied
     *  - 401: Not authenticated
     * 
    * @return [Call]<[CommonToken]>
     */
    @GET("client/login")
    fun clientLoginGet(): Call<CommonToken>

    /**
     * Lists details of the organization // no authentication required
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[ForClientOrganization]>
     */
    @GET("client/organization")
    fun clientOrganizationGet(): Call<ForClientOrganization>

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
    fun clientRegisterPost(@Body commonClient: CommonClient): Call<CommonClient>

    /**
     * Share their personal/client data with the organization //client is registered by employee
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[Unit]>
     */
    @GET("client/shareDataWithOrganization")
    fun clientShareDataWithOrganizationGet(): Call<Unit>

}
