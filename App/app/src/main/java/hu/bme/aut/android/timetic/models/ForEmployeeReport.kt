/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Timetic szervezeti backend
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package hu.bme.aut.android.timetic.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property sumOnlineHours
 * @property sumLocalHours
 * @property sumOnlineIncome
 * @property sumLocalIncome
 * @property activities
 */
@JsonClass(generateAdapter = true)
data class ForEmployeeReport(
    @Json(name = "sumOnlineHours") @field:Json(name = "sumOnlineHours") var sumOnlineHours: Double? = null,
    @Json(name = "sumLocalHours") @field:Json(name = "sumLocalHours") var sumLocalHours: Double? = null,
    @Json(name = "sumOnlineIncome") @field:Json(name = "sumOnlineIncome") var sumOnlineIncome: Double? = null,
    @Json(name = "sumLocalIncome") @field:Json(name = "sumLocalIncome") var sumLocalIncome: Double? = null,
    @Json(name = "activities") @field:Json(name = "activities") var activities: List<ForEmployeeActivityForReport>? = null
)