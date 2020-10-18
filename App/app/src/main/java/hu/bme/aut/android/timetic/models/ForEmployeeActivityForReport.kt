/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Timetic szervezeti backend
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package hu.bme.aut.android.timetic.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property name
 * @property sumHours
 * @property sumIncome
 */
@JsonClass(generateAdapter = true)
data class ForEmployeeActivityForReport(
    @Json(name = "name") @field:Json(name = "name") var name: String? = null,
    @Json(name = "sumHours") @field:Json(name = "sumHours") var sumHours: Double? = null,
    @Json(name = "sumIncome") @field:Json(name = "sumIncome") var sumIncome: Double? = null
)