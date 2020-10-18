/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Timetic szervezeti backend
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package hu.bme.aut.android.timetic.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property activities
 * @property clients
 * @property places
 */
@JsonClass(generateAdapter = true)
data class ForEmployeeDataForAppointmentCreation(
    @Json(name = "activities") @field:Json(name = "activities") var activities: List<CommonActivity>? = null,
    @Json(name = "clients") @field:Json(name = "clients") var clients: List<CommonClient>? = null,
    @Json(name = "places") @field:Json(name = "places") var places: List<String>? = null
)
