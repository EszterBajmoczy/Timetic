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
 * @param clientPersonalInfoFields 
 */

data class ForEmployeeOrganization (
    @Json(name = "id")
    val id: kotlin.String? = null,
    @Json(name = "clientPersonalInfoFields")
    val clientPersonalInfoFields: kotlin.collections.List<kotlin.String>? = null
)

