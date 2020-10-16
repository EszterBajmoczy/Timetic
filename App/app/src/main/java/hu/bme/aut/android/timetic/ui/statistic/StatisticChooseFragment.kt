package hu.bme.aut.android.timetic.ui.statistic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.datepicker.MaterialDatePicker
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.SettingsActivity
import kotlinx.android.synthetic.main.fragment_statistic.*
import java.util.*

class StatisticChooseFragment : Fragment() {

    private lateinit var statisticViewModel: StatisticViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        statisticViewModel =
            ViewModelProviders.of(this).get(StatisticViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_statistic, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)

        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.statistic_month_entries,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                date_spinner.adapter = adapter
            }
        }

        btSelectDateRange.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val now = Calendar.getInstance()
            builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))

            val picker = builder.build()
            picker.show(activity?.supportFragmentManager!!, picker.toString())

            picker.addOnNegativeButtonClickListener { TODO() }
            picker.addOnPositiveButtonClickListener { TODO() }
        }

    }
}
