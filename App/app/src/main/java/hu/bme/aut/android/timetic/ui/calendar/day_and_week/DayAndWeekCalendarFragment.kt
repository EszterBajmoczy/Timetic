package hu.bme.aut.android.timetic.ui.calendar.day_and_week

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.alamkanak.weekview.*
import hu.bme.aut.android.timetic.create.NewAppointmentActivity
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModel


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
        viewModel = ViewModelProviders.of(this).get(CalendarViewModel::class.java)


        weekView = requireActivity().findViewById<WeekView<Appointment>>(R.id.weekView)
        // TODO: Use the ViewModel
        viewModel.apps.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            weekView.submit(it)
        })

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "secure_shared_preferences",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        viewModel.clients.observe(viewLifecycleOwner, Observer {
            viewModel.downloadAppointments(secureSharedPreferences)
        })

        //set viewType
        setCalendarType()

        //TODO
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

    fun setCalendarType(){
        when (type){
            ViewType.Daily ->
                weekView.numberOfVisibleDays = 1
            ViewType.Weekly ->
                weekView.numberOfVisibleDays = 7
        }
    }
}


