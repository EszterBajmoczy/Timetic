package hu.bme.aut.android.timetic.ui.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.NewAppointmentActivity
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.ui.calendar.month.MonthCalendarFragment
import hu.bme.aut.android.timetic.ui.calendar.day_and_week.DayAndWeekCalendarFragment
import kotlinx.android.synthetic.main.fragment_calendar_main.*


class CalendarMainFragment : Fragment() {
    private var myContext: FragmentActivity? = null
    private lateinit var calendarViewModel: CalendarViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_calendar_main, container, false)


        return root
    }

    override fun onAttach(activity: Activity) {
        myContext = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        calendarViewModel = ViewModelProviders.of(this).get(CalendarViewModel::class.java)
        // TODO: Use the ViewModel

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

            calendarFragment.type = DayAndWeekCalendarFragment.ViewType.Weekly


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

            calendarFragment.type = DayAndWeekCalendarFragment.ViewType.Daily

            val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

            fragManager
                .beginTransaction()
                .replace(R.id.frag_container, calendarFragment)
                .commit()

            btMonthly.setBackgroundResource(R.color.colorPrimaryLight)
            btWeekly.setBackgroundResource(R.color.colorPrimaryLight)
            btDaily.setBackgroundResource(R.color.colorPrimary)
        }

    }

    fun setFloatingActionButton(){
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(activity, NewAppointmentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setDefaultFragment(defaultView: String?) {
        when(defaultView) {
            "Napi" ->
                {
                    val calendarFragment =
                        DayAndWeekCalendarFragment()

                    calendarFragment.type = DayAndWeekCalendarFragment.ViewType.Daily

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

                    calendarFragment.type = DayAndWeekCalendarFragment.ViewType.Weekly

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
