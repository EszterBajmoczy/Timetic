/**
* Timetic szervezeti backend
* No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
*
* The version of the OpenAPI document: 1.0.0
* Contact: you@your-company.com
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package hu.bme.aut.android.timetic.network.models


import com.squareup.moshi.Json

/**
 * 
 * @param id 
 * @param isPrivate 
 * @param startTime 
 * @param endTime 
 * @param client 
 * @param activity 
 * @param place 
 * @param price 
 * @param online 
 * @param note 
 */

data class CommonAppointment (
    @Json(name = "id")
    val id: kotlin.String? = null,
    @Json(name = "isPrivate")
    val isPrivate: kotlin.Boolean? = null,
    @Json(name = "startTime")
    val startTime: kotlin.Long? = null,
    @Json(name = "endTime")
    val endTime: kotlin.Long? = null,
    @Json(name = "client")
    val client: CommonClient? = null,
    @Json(name = "activity")
    val activity: CommonActivity? = null,
    @Json(name = "employee")
    val employee: CommonEmployee? = null,
    @Json(name = "place")
    val place: kotlin.String? = null,
    @Json(name = "price")
    val price: kotlin.Double? = null,
    @Json(name = "online")
    val online: kotlin.Boolean? = null,
    @Json(name = "note")
    val note: kotlin.String? = null
)

