package hu.bme.aut.android.timetic.ui.statistic

import android.app.Activity
import android.os.Bundle
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

        btChooseTimeRange.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val now = Calendar.getInstance()
            builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))

            val picker = builder.build()
            picker.show(activity?.supportFragmentManager!!, picker.toString())

            picker.addOnPositiveButtonClickListener {
                viewModel.fetchData(it.first!!, it.second!!, MyApplication.getOrganisationUrl()!!, MyApplication.getToken()!!)
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

    override fun onDestroyView() {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = true
        super.onDestroyView()
    }
}
