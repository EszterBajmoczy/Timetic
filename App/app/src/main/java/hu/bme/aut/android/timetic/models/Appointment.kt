package hu.bme.aut.android.timetic.models

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import hu.bme.aut.android.timetic.R
import java.lang.Exception
import java.util.*

class Appointment(
    val id: Long?,
    val backendId: String,
    val note: String?,
    val start_date: Calendar,
    val end_date: Calendar,
    val price: Double? = null,
    val private_appointment: Boolean,
    val videochat: Boolean?,
    val location: String?,
    val personBackendId: String?,
    val activity: String?,
    var organizationUrl: String? = null
) : Comparable<Appointment> {

    override fun compareTo(other: Appointment): Int {
        return start_date.timeInMillis.compareTo(other.start_date.timeInMillis)
    }
}