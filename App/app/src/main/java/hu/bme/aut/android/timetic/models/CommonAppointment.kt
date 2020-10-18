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
 * @property isPrivate
 * @property startTime
 * @property endTime
 * @property client
 * @property activity
 * @property place
 * @property price
 * @property online
 * @property note
 */
@JsonClass(generateAdapter = true)
data class CommonAppointment(
    @Json(name = "id") @field:Json(name = "id") var id: String? = null,
    @Json(name = "isPrivate") @field:Json(name = "isPrivate") var isPrivate: Boolean? = null,
    @Json(name = "startTime") @field:Json(name = "startTime") var startTime: Double? = null,
    @Json(name = "endTime") @field:Json(name = "endTime") var endTime: Double? = null,
    @Json(name = "client") @field:Json(name = "client") var client: CommonClient? = null,
    @Json(name = "activity") @field:Json(name = "activity") var activity: CommonActivity? = null,
    @Json(name = "place") @field:Json(name = "place") var place: String? = null,
    @Json(name = "price") @field:Json(name = "price") var price: Double? = null,
    @Json(name = "online") @field:Json(name = "online") var online: Boolean? = null,
    @Json(name = "note") @field:Json(name = "note") var note: String? = null
)