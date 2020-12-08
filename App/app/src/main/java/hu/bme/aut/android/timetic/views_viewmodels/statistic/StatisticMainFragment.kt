package hu.bme.aut.android.timetic.views_viewmodels.statistic

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import kotlinx.android.synthetic.main.fragment_statistic_main.*
import java.lang.Exception
import java.util.*


class StatisticMainFragment : Fragment() {
    private var myContext: FragmentActivity? = null
    private lateinit var viewModel: StatisticDiagramViewModel

    private val internetStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                tNoInternetConnectionStatistic.visibility = View.GONE
                btChooseTimeRangeContainer.visibility = View.VISIBLE
                initialize()
                context.unregisterReceiver(this)
            }
        }
    }

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

        //check internet connection
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if(isNetworkAvailable){
            initialize()
        } else {
            tNoInternetConnectionStatistic.visibility = View.VISIBLE
            btChooseTimeRangeContainer.visibility = View.GONE
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context?.registerReceiver(internetStateChangedReceiver, intentFilter)
        }
    }

    fun initialize() {
        viewModel = ViewModelProviders.of(requireActivity()).get(StatisticDiagramViewModel::class.java)

        btChooseTimeRange.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val now = Calendar.getInstance()
            builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))

            val picker = builder.build()
            picker.show(activity?.supportFragmentManager!!, picker.toString())

            picker.addOnPositiveButtonClickListener {
                viewModel.fetchData(correctDate(it.first!!, "Begin"), correctDate(it.second!!, "End"), MyApplication.getOrganizationUrl()!!, MyApplication.getToken()!!)
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

    //to modify the begin and and dates to be at 00:00 and at 23:59
    private fun correctDate(value: Long, type: String): Long {
        val date = Calendar.getInstance()
        date.timeInMillis = value

        when(type) {
            "Begin" -> {
                date.set(Calendar.HOUR_OF_DAY, 0)
                date.set(Calendar.MINUTE, 0)
                date.set(Calendar.MILLISECOND, 0)
            }
            "End" -> {
                date.set(Calendar.HOUR_OF_DAY, 23)
                date.set(Calendar.MINUTE, 59)
                date.set(Calendar.MILLISECOND, 59)
            }
        }
        return date.timeInMillis
    }

    override fun onPause() {
        try {
            context?.unregisterReceiver(internetStateChangedReceiver)
        } catch (e: Exception) {}
        super.onPause()
    }

    override fun onDestroyView() {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = true
        super.onDestroyView()
    }
}
