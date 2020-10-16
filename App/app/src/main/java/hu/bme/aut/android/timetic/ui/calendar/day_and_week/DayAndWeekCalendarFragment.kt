package hu.bme.aut.android.timetic.ui.calendar.day_and_week

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.alamkanak.weekview.*
import hu.bme.aut.android.timetic.NewAppointmentActivity
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.ui.calendar.Event
import java.util.*
import kotlin.collections.ArrayList


class DayAndWeekCalendarFragment : Fragment() {

    companion object {
        fun newInstance() =
            DayAndWeekCalendarFragment()
    }
    private lateinit var weekView: WeekView<Appointment>
    private lateinit var viewModel: DayAndWeekCalendarViewModel
    enum class ViewType { Daily, Weekly }
    lateinit var type : ViewType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.day_and_week_calendar_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DayAndWeekCalendarViewModel::class.java)
        // TODO: Use the ViewModel
        weekView = requireActivity().findViewById<WeekView<Appointment>>(R.id.weekView)

        //set viewType
        setCalendarType()

        //set events
        val events: List<WeekViewDisplayable<Appointment>> = onMonthChange(2020, 10)
        weekView.submit(events)

        //set listeners
        weekView.setOnEventClickListener { data, rect ->
            Toast.makeText(context, "Event  clicked: ", Toast.LENGTH_SHORT).show()
        }

        weekView.setOnEventLongClickListener { data, rect ->
            Toast.makeText(context, "Event  long clicked: ", Toast.LENGTH_SHORT).show()
        }

        weekView.setOnEmptyViewLongClickListener {
            val intent = Intent(activity, NewAppointmentActivity::class.java)
            startActivity(intent)
        }
        weekView.setOnRangeChangeListener { firstVisibleDate, lastVisibleDate ->  }

        weekView.setOnRangeChangeListener { firstVisibleDate, lastVisibleDate ->
            Toast.makeText(context, "weekchanged: ", Toast.LENGTH_SHORT).show()

        }

    }

    fun onMonthChange(newYear: Int, newMonth: Int): List<WeekViewDisplayable<Appointment>> {
        //TODO set colors correctly
        val events: MutableList<Appointment> = ArrayList()

        var startTime = Calendar.getInstance()
        startTime[Calendar.HOUR_OF_DAY] = 3
        startTime[Calendar.MINUTE] = 0
        startTime[Calendar.MONTH] = newMonth - 1
        startTime[Calendar.YEAR] = newYear
        var endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR, 1)
        endTime[Calendar.MONTH] = newMonth - 1
        var event = Appointment(1, "App1", startTime, endTime, 1000,false, false, "address", 2, 2)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime[Calendar.DAY_OF_MONTH] = 15
        startTime[Calendar.HOUR_OF_DAY] = 3
        startTime[Calendar.MINUTE] = 0
        startTime[Calendar.MONTH] = newMonth - 1
        startTime[Calendar.YEAR] = newYear
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = Appointment(2, "App2", startTime, endTime, 1000,false, false, "address", 2, 2)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime[Calendar.DAY_OF_MONTH] = 15
        startTime[Calendar.HOUR_OF_DAY] = 4
        startTime[Calendar.MINUTE] = 0
        startTime[Calendar.MONTH] = newMonth - 1
        startTime[Calendar.YEAR] = newYear
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = Appointment(3, "App3", startTime, endTime, 1000,false, false, "address", 2, 2)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime[Calendar.DAY_OF_MONTH] = 16
        startTime[Calendar.HOUR_OF_DAY] = 10
        startTime[Calendar.MINUTE] = 0
        startTime[Calendar.MONTH] = newMonth - 1
        startTime[Calendar.YEAR] = newYear
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = Appointment(4, "App4", startTime, endTime, 1000,false, false, "address", 2, 2)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime[Calendar.DAY_OF_MONTH] = 14
        startTime[Calendar.HOUR_OF_DAY] = 0
        startTime[Calendar.MINUTE] = 20
        startTime[Calendar.MONTH] = newMonth - 1
        startTime[Calendar.YEAR] = newYear
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = Appointment(5, "App5", startTime, endTime, 1000,false, false, "address", 2, 2)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime[Calendar.DAY_OF_MONTH] = 14
        startTime[Calendar.HOUR_OF_DAY] = 23
        startTime[Calendar.MINUTE] = 20
        startTime[Calendar.MONTH] = newMonth - 1
        startTime[Calendar.YEAR] = newYear
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = Appointment(6, "App6", startTime, endTime, 1000,false, false, "address", 2, 2)

        events.add(event)

        return events
    }

    fun setCalendarType(){
        when (type){
            ViewType.Daily ->
                weekView.numberOfVisibleDays = 1
            ViewType.Weekly ->
                weekView.numberOfVisibleDays = 7
        }
    }
}


