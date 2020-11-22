package hu.bme.aut.android.timetic.ui.calendar.day_and_week

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alamkanak.weekview.WeekView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.create.NewAppointmentActivity
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModel
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModelFactory
import java.util.*


class DayAndWeekCalendarFragment : Fragment() {

    companion object {
        fun newInstance() =
            DayAndWeekCalendarFragment()
    }
    private lateinit var weekView: WeekView<Appointment>
    private lateinit var viewModel: CalendarViewModel
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
        viewModel = ViewModelProvider(requireActivity(), CalendarViewModelFactory()).get(CalendarViewModel::class.java)
        Log.d("EZAZ", "fragment")

        weekView = requireActivity().findViewById(R.id.weekView)
        val calendar = Calendar.getInstance()
        weekView.goToHour(calendar.get(Calendar.HOUR_OF_DAY))
        viewModel.result.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let{
                weekView.submit(it)
            }
        })

        if(!viewModel.clientResult.hasObservers()){
            viewModel.clientResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                //it needs an observer to be able to save the clients
            })
        }

        //set viewType
        val bundle = this.arguments
        if (bundle != null) {
            val type = bundle.getString("CalendarType", "Weekly")
            setCalendarType(type)
        }

        //set listeners
        weekView.setOnEventClickListener { data, rect ->
            val intent = Intent(context, NewAppointmentActivity::class.java)
            intent.putExtra("NetId", data.backendId)
            startActivity(intent)
        }

        weekView.setOnEventLongClickListener { data, rect ->
            val intent = Intent(context, NewAppointmentActivity::class.java)
            intent.putExtra("NetId", data.backendId)
            startActivity(intent)
        }

        weekView.setOnEmptyViewLongClickListener {
            val intent = Intent(activity, NewAppointmentActivity::class.java)
            startActivity(intent)
        }
    }

    fun setCalendarType(type: String){
        when (type){
            "Daily" ->
                weekView.numberOfVisibleDays = 1
            "Weekly" ->
                weekView.numberOfVisibleDays = 7
        }
    }
}


