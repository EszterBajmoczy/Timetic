package hu.bme.aut.android.timetic.ui.statistic

import android.R.attr.fragment
import android.R.attr.key
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
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.create.NewAppointmentActivity
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModel
import hu.bme.aut.android.timetic.ui.calendar.day_and_week.DayAndWeekCalendarFragment
import hu.bme.aut.android.timetic.ui.calendar.month.MonthCalendarFragment
import kotlinx.android.synthetic.main.fragment_calendar_main.*
import kotlinx.android.synthetic.main.fragment_statistic_main.*
import java.util.*


class StatisticMainFragment : Fragment() {
    private var myContext: FragmentActivity? = null
    private lateinit var viewModel: StatisticDiagramViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistic_main, container, false)
    }

    override fun onAttach(activity: Activity) {
        myContext = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = false

        viewModel = ViewModelProviders.of(requireActivity()).get(StatisticDiagramViewModel::class.java)

        val sp = PreferenceManager.getDefaultSharedPreferences (context)
        val defaultView = sp.getString("calendarType","-1")

        btChooseTimeRange.setOnClickListener {
            //TODO select time

            val secureSharedPreferences = MyApplication.secureSharedPreferences

            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val now = Calendar.getInstance()
            builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))

            val picker = builder.build()
            picker.show(activity?.supportFragmentManager!!, picker.toString())

            picker.addOnNegativeButtonClickListener {
                //Todo nothing?
            }
            picker.addOnPositiveButtonClickListener {
                viewModel.fetchData(it.first!!, it.second!!, secureSharedPreferences.getString("OrganisationUrl", "").toString(), secureSharedPreferences.getString("Token", "").toString())
                val statisticDiagramFragment =
                    StatisticDiagramFragment()

                val fragManager: androidx.fragment.app.FragmentManager = childFragmentManager

                fragManager
                    .beginTransaction()
                    .replace(R.id.frag_container_stat, statisticDiagramFragment)
                    .commit()
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
                        .replace(R.id.frag_container_stat, calendarFragment)
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
                        .replace(R.id.frag_container_stat, calendarFragment)
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
                        .replace(R.id.frag_container_stat, calendarFragment)
                        .commit()

                    btMonthly.setBackgroundResource(R.color.colorPrimary)
                    btWeekly.setBackgroundResource(R.color.colorPrimaryLight)
                    btDaily.setBackgroundResource(R.color.colorPrimaryLight)
                }
        }
    }
    override fun onDestroyView() {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = true
        super.onDestroyView()
    }
}
