/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Timetic szervezeti backend
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package hu.bme.aut.android.timetic.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property id
 * @property startTime
 * @property endTime
 * @property employee
 * @property activity
 * @property place
 * @property price
 * @property online
 * @property note
 */
@JsonClass(generateAdapter = true)
data class ForClientAppointment(
    @Json(name = "id") @field:Json(name = "id") var id: String? = null,
    @Json(name = "startTime") @field:Json(name = "startTime") var startTime: Double? = null,
    @Json(name = "endTime") @field:Json(name = "endTime") var endTime: Double? = null,
    @Json(name = "employee") @field:Json(name = "employee") var employee: CommonEmployee? = null,
    @Json(name = "activity") @field:Json(name = "activity") var activity: CommonActivity? = null,
    @Json(name = "place") @field:Json(name = "place") var place: String? = null,
    @Json(name = "price") @field:Json(name = "price") var price: Double? = null,
    @Json(name = "online") @field:Json(name = "online") var online: Boolean? = null,
    @Json(name = "note") @field:Json(name = "note") var note: String? = null
)