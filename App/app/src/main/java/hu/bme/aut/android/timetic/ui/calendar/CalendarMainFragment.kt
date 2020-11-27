package hu.bme.aut.android.timetic.ui.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.create.NewAppointmentActivity
import hu.bme.aut.android.timetic.ui.calendar.day_and_week.DayAndWeekCalendarFragment
import hu.bme.aut.android.timetic.ui.calendar.month.MonthCalendarFragment
import kotlinx.android.synthetic.main.fragment_calendar_main.*
import java.text.SimpleDateFormat
import java.util.*


class CalendarMainFragment : Fragment() {
    private var myContext: FragmentActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar_main, container, false)
    }

    override fun onAttach(activity: Activity) {
        myContext = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //set floating action button
        setFloatingActionButton()

        val sp = PreferenceManager.getDefaultSharedPreferences (context)
        val defaultView = sp.getString("calendarType","-1")

        setDefaultFragment(defaultView)

        btMonthly.setOnClickListener {
            val calendarFragment =
                MonthCalendarFragment()


            val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

            fragManager
                .beginTransaction()
                .replace(R.id.frag_container, calendarFragment)
                .commit()

            btMonthly.setBackgroundResource(R.color.colorPrimary)
            btWeekly.setBackgroundResource(R.color.colorPrimaryLight)
            btDaily.setBackgroundResource(R.color.colorPrimaryLight)
        }

        btWeekly.setOnClickListener {
            val calendarFragment =
                DayAndWeekCalendarFragment()
            val bundle = Bundle()
            bundle.putString("CalendarType", "Weekly")
            calendarFragment.arguments = bundle

            val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

            fragManager
                .beginTransaction()
                .replace(R.id.frag_container, calendarFragment)
                .commit()

            btMonthly.setBackgroundResource(R.color.colorPrimaryLight)
            btWeekly.setBackgroundResource(R.color.colorPrimary)
            btDaily.setBackgroundResource(R.color.colorPrimaryLight)
        }

        btDaily.setOnClickListener {
            val calendarFragment =
                DayAndWeekCalendarFragment()

            val bundle = Bundle()
            bundle.putString("CalendarType", "Daily")
            calendarFragment.arguments = bundle

            val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

            fragManager
                .beginTransaction()
                .replace(R.id.frag_container, calendarFragment)
                .commit()

            btMonthly.setBackgroundResource(R.color.colorPrimaryLight)
            btWeekly.setBackgroundResource(R.color.colorPrimaryLight)
            btDaily.setBackgroundResource(R.color.colorPrimary)
        }

        checkLastSynchronizationDate()

    }

    private fun checkLastSynchronizationDate() {
        val pref = MyApplication.secureSharedPreferences
        val yesterday = Calendar.getInstance()
        yesterday.set(Calendar.DAY_OF_MONTH, -1)
        val syncDate = Calendar.getInstance()

        syncDate.timeInMillis = pref.getLong("LastSync", 0)

        if(syncDate.timeInMillis < yesterday.timeInMillis && syncDate.timeInMillis != 0L){
            val simpleFormat = SimpleDateFormat("yyyy.MM.dd\nHH:mm", Locale.getDefault())
            tLastSynchronized.text = getString(R.string.last_sync) + simpleFormat.format(syncDate.time)
            tLastSynchronized.visibility = View.VISIBLE
        } else {
            tLastSynchronized.visibility = View.GONE
        }
    }

    private fun setFloatingActionButton(){
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                val intent = Intent(activity, NewAppointmentActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(context, getString(R.string.network_needed_new_appointment), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setDefaultFragment(defaultView: String?) {
        when(defaultView) {
            "Napi" ->
                {
                    val calendarFragment =
                        DayAndWeekCalendarFragment()

                    val bundle = Bundle()
                    bundle.putString("CalendarType", "Daily")
                    calendarFragment.arguments = bundle

                    val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

                    fragManager
                        .beginTransaction()
                        .replace(R.id.frag_container, calendarFragment)
                        .commit()

                    btMonthly.setBackgroundResource(R.color.colorPrimaryLight)
                    btWeekly.setBackgroundResource(R.color.colorPrimaryLight)
                    btDaily.setBackgroundResource(R.color.colorPrimary)
                }
            "Heti" ->
                {
                    val calendarFragment =
                        DayAndWeekCalendarFragment()

                    val bundle = Bundle()
                    bundle.putString("CalendarType", "Weekly")
                    calendarFragment.arguments = bundle

                    val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

                    fragManager
                        .beginTransaction()
                        .replace(R.id.frag_container, calendarFragment)
                        .commit()

                    btMonthly.setBackgroundResource(R.color.colorPrimaryLight)
                    btWeekly.setBackgroundResource(R.color.colorPrimary)
                    btDaily.setBackgroundResource(R.color.colorPrimaryLight)
                }
            "Havi" ->
                {
                    val calendarFragment =
                        MonthCalendarFragment()


                    val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

                    fragManager
                        .beginTransaction()
                        .replace(R.id.frag_container, calendarFragment)
                        .commit()

                    btMonthly.setBackgroundResource(R.color.colorPrimary)
                    btWeekly.setBackgroundResource(R.color.colorPrimaryLight)
                    btDaily.setBackgroundResource(R.color.colorPrimaryLight)
                }
        }
    }
}
