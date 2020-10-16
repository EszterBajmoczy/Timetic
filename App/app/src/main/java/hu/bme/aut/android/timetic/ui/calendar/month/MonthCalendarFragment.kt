package hu.bme.aut.android.timetic.ui.calendar.month

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.ui.calendar.CurrentDayDecorator
import kotlinx.android.synthetic.main.month_calendar_fragment.*


class MonthCalendarFragment : Fragment() {

    companion object {
        fun newInstance() =
            MonthCalendarFragment()
    }

    private lateinit var viewModel: MonthCalendarViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.month_calendar_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MonthCalendarViewModel::class.java)
        // TODO: Use the ViewModel

        //TODO egymásra rajzolást kijavítani
        lateinit var widget: MaterialCalendarView
        val activity = getActivity()
        widget = view?.findViewById(R.id.calendarMonthView) as MaterialCalendarView

        val mydate= CalendarDay.from(2020,  10, 15) // year, month, date
        val mydate0= CalendarDay.from(2020,  10, 14) // year, month, date
        val mydate1= CalendarDay.from(2020,  10, 25) // year, month, date
        widget.addDecorators(CurrentDayDecorator(activity, mydate))
        widget.addDecorators(CurrentDayDecorator(activity, mydate))
        widget.addDecorators(CurrentDayDecorator(activity, mydate0))
        widget.addDecorators(CurrentDayDecorator(activity, mydate1))

        calendarMonthView.setOnDateLongClickListener { widget, date ->
            Log.e("Yesss", "onDateClick:" + date.toString())
            // TODO new event
        }

        //TODO list events under the calendar
    }

}
