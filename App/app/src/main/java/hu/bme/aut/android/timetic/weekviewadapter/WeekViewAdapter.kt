package hu.bme.aut.android.timetic.weekviewadapter

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.WeekViewEvent
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.models.Appointment
import java.util.*
import kotlin.reflect.KFunction1

class WeekViewAdapter(val callBack: KFunction1<@ParameterName(name = "backendId") String?, Unit>) : WeekView.SimpleAdapter<Appointment>() {

    override fun onCreateEntity(item: Appointment): WeekViewEntity {
        val title: String = item.activity
            ?: if(item.note != null && item.note != ""){
                item.note
            } else{
                "Névtelen"
            }

        val style = if(item.private_appointment) {
            WeekViewEntity.Style.Builder()
                .setBackgroundColorResource(R.color.colorAccentLight)
                .build()
        } else{
            WeekViewEntity.Style.Builder()
                .setBackgroundColorResource(R.color.colorAccent)
                .build()
        }

        return WeekViewEntity.Event.Builder(item)
        .setId(item.id!!)
        .setTitle(title)
        .setStartTime(item.start_date)
        .setEndTime(item.end_date)
        .setStyle(style)
        .build()
    }

    override fun onEventClick(data: Appointment) {
        callBack(data.backendId)
    }

    override fun onEventLongClick(data: Appointment) {
        callBack(data.backendId)
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        callBack(null)
    }
}