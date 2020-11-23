package hu.bme.aut.android.timetic.data.model

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import hu.bme.aut.android.timetic.R
import java.lang.Exception
import java.util.*
import kotlin.random.Random.Default.nextInt

class Appointment(
    val id: Long?,
    val backendId: String,
    val note: String?,
    val start_date: Calendar,
    val end_date: Calendar,
    val price: Double? = null,
    val private_appointment: Boolean,
    val videochat: Boolean?,
    val address: String?,
    val personBackendId: String?,
    val activity: String?,
    var organizationUrl: String? = null
) : WeekViewDisplayable<Appointment>, Comparable<Appointment> {

    override fun toWeekViewEvent(): WeekViewEvent<Appointment> {
        // Build the styling of the event, for instance background color and strike-through
        val style = if(private_appointment) {
            WeekViewEvent.Style.Builder()
                .setBackgroundColorResource(R.color.colorAccentLight)
                .build()
        } else{
            WeekViewEvent.Style.Builder()
                .setBackgroundColorResource(R.color.colorAccent)
                .build()
        }
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

        while(true) {
            try {
                return WeekViewEvent.Builder<Appointment>(this)
                    .setId((0..100000).random().toLong())
                    .setTitle(title)
                    .setStartTime(start_date)
                    .setEndTime(end_date)
                    .setStyle(style)
                    .build()
            } catch (e: Exception) {}
        }
    }

    override fun compareTo(other: Appointment): Int {
        return start_date.timeInMillis.compareTo(other.start_date.timeInMillis)
    }
}