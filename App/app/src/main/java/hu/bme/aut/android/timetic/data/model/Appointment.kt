package hu.bme.aut.android.timetic.data.model

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import hu.bme.aut.android.timetic.R
import java.util.*

class Appointment(
    val id: Long?,
    val netId: String,
    val note: String?,
    val start_date: Calendar,
    val end_date: Calendar,
    val price: Double? = null,
    val private_appointment: Boolean,
    val videochat: Boolean?,
    val address: String?,
    val person: String?,
    val personPhone: String?,
    val personEmail: String?,
    val activity: String?,
    var organisationUrl: String? = null
) : WeekViewDisplayable<Appointment>, Comparable<Appointment> {

    override fun toWeekViewEvent(): WeekViewEvent<Appointment> {
        // Build the styling of the event, for instance background color and strike-through
        val style = WeekViewEvent.Style.Builder()
            .setBackgroundColor(R.color.colorPrimaryLight)
            .build()
        // Build the WeekViewEvent via the Builder
        val title: String
        if(activity != null){
            title = activity
        }
        else if(note != null && note != ""){
            title = note
        }
        else{
            title = "Event"
        }

        return WeekViewEvent.Builder<Appointment>(this)
            .setId(id!!)
            .setTitle(title)
            .setStartTime(start_date)
            .setEndTime(end_date)
            .setStyle(style)
            .build()

    }

    override fun compareTo(other: Appointment): Int {
        return start_date.timeInMillis.compareTo(other.start_date.timeInMillis)
    }
}