package hu.bme.aut.android.timetic.network.apiDeveloper

import retrofit2.http.*
import retrofit2.Call

import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.network.models.ForMobileUserRegistration

interface MobileApi {
    /**
     * Logs client into the system
     * 
     * Responses:
     *  - 200: successful operation
     *  - 404: Invalid username/password supplied
     *  - 401: Not authenticated
     * 
    * @return [Call]<[CommonToken]>
     */
    @GET("mobile/login")
    fun mobileLoginGet(): Call<CommonToken>

    /**
     * Logs  mobile user out of the system
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[Unit]>
     */
    @GET("mobile/logout")
    fun mobileLogoutGet(): Call<Unit>

    /**
     * Lists registered organizations // no credentials needed!!!!!!
     * 
     * Responses:
     *  - 200: successful operation
     * 
    * @return [Call]<[kotlin.collections.List<CommonOrganization>]>
     */
    @GET("mobile/organizations")
    fun mobileOrganizationsGet(): Call<kotlin.collections.List<CommonOrganization>>

    /**
     * Refresh mobile user&#39;s token
     * 
     * Responses:
     *  - 200: successful operation
     *  - 400: Invalid username/password supplied
     *  - 401: Not authenticated
     *  - 403: Access token does not have the required scope
     * 
    * @return [Call]<[CommonToken]>
     */
    @GET("mobile/refresh")
    fun mobileRefreshGet(): Call<CommonToken>

    /**
     * Registers mobile user
     * 
     * Responses:
     *  - 201: successfully created
     * 
     * @param forMobileUserRegistration  
    * @return [Call]<[Unit]>
     */
    @POST("mobile/register")
    fun mobileRegisterPost(@Body forMobileUserRegistration: ForMobileUserRegistration): Call<Unit>

}
