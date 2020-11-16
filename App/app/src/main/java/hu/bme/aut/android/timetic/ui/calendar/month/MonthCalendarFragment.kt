package hu.bme.aut.android.timetic.ui.calendar.month

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.adapter.AppointmentAdapter
import hu.bme.aut.android.timetic.create.NewAppointmentActivity
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModel
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModelFactory
import hu.bme.aut.android.timetic.ui.calendar.CurrentDayDecorator
import kotlinx.android.synthetic.main.month_calendar_fragment.*
import java.util.*


class MonthCalendarFragment : Fragment(), AppointmentAdapter.AppointmentItemClickListener {

    companion object {
        fun newInstance() =
            MonthCalendarFragment()
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.month_calendar_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), CalendarViewModelFactory()).get(CalendarViewModel::class.java)
        // TODO: Use the ViewModel

        val calendarView = view?.findViewById(R.id.calendarMonthView) as MaterialCalendarView
        viewModel.result.observe(viewLifecycleOwner, Observer {
            calendarView.removeDecorators()
            for(appointment in it){
                val date = CalendarDay.from(appointment.start_date.get(Calendar.YEAR),  appointment.start_date.get(Calendar.MONTH) + 1, appointment.start_date.get(Calendar.DAY_OF_MONTH))
                calendarView.addDecorators(CurrentDayDecorator(requireActivity(), date))
            }
            calendarView.setOnDateChangedListener { widget, date, selected ->
                dateMonthView.text = date.day.toString()
                //TODO list events under the calendar
                val list = ArrayList<Appointment>()
                for(item in it) {
                    val appointmentDate = CalendarDay.from(item.start_date.get(Calendar.YEAR),  item.start_date.get(Calendar.MONTH) + 1, item.start_date.get(Calendar.DAY_OF_MONTH))
                    if(date == appointmentDate){
                        list.add(item)
                    }
                }
                adapter.update(list)
            }
        })
        viewModel.clientResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            //it needs an observer to be able to save the clients
        })

        calendarView.setOnDateLongClickListener { widget, date ->
            val intent = Intent(activity, NewAppointmentActivity::class.java)
            startActivity(intent)
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = MainRecyclerView
        adapter = AppointmentAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onItemClick(appointment: Appointment) {
        val intent = Intent(context, NewAppointmentActivity::class.java)
        intent.putExtra("NetId", appointment.netId)
        startActivity(intent)
    }

}

fun Appointment.compare() {

}
