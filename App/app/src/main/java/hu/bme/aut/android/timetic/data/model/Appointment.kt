package hu.bme.aut.android.timetic.data.model

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import hu.bme.aut.android.timetic.R
import java.util.*

class Appointment(
    val id: Long,
    val details: String,
    val start_date: Calendar,
    val end_date: Calendar,
    val price: Int? = null,
    val private_appointment: Boolean,
    val videochat: Boolean,
    val address: String,
    val client_id: Int?,
    val activity_id: Int?
) : WeekViewDisplayable<Appointment> {

    override fun toWeekViewEvent(): WeekViewEvent<Appointment> {
        // Build the styling of the event, for instance background color and strike-through
        val style = WeekViewEvent.Style.Builder()
            .setBackgroundColor(R.color.colorPrimaryLight)
            .build()
        // Build the WeekViewEvent via the Builder
        //TODO id, title!
        return WeekViewEvent.Builder<Appointment>(this)
            .setId(id)
            .setTitle(details)
            .setStartTime(start_date)
            .setEndTime(end_date)
            .setStyle(style)
            .build()

    }

}