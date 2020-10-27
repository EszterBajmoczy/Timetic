package hu.bme.aut.android.timetic.ui.statistic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.R
import kotlinx.android.synthetic.main.statistic_diagram_fragment.*
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import java.util.*


class StatisticDiagramFragment : Fragment() {
    var pieChart1: PieChart? = null
    var pieChart2: PieChart? = null


    companion object {
        fun newInstance() = StatisticDiagramFragment()
    }

    private lateinit var viewModel: StatisticDiagramViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = false

        return inflater.inflate(R.layout.statistic_diagram_fragment, container, false)
    }

    override fun onDestroyView() {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = true

        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StatisticDiagramViewModel::class.java)
        // TODO: Use the ViewModel
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "secure_shared_preferences",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()
        builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))

        val picker = builder.build()
        picker.show(activity?.supportFragmentManager!!, picker.toString())

        picker.addOnNegativeButtonClickListener {
            //Todo do better
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            }
        picker.addOnPositiveButtonClickListener {
            viewModel.fetchData(it.first!!, it.second!!, secureSharedPreferences.getString("OrganisationUrl", "").toString(), secureSharedPreferences.getString("Token", "").toString())
        }
        pieChart1 = piechart1
        pieChart2 = piechart2

        viewModel.data.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            setDataPieChart1(it.sumLocalHours!!, it.sumOnlineHours!!)
            setDataPieChart2(it.sumLocalIncome!!, it.sumOnlineIncome!!)
        })
    }

    private fun setDataPieChart1(sumLocalHours: Double, sumOnlineHours: Double) {
        pieChart1!!.addPieSlice(
            PieModel(
                "Személyes időpontok",
                sumLocalHours.toFloat(),
                Color.parseColor("#FF6B6B")
            )
        )
        pieChart1!!.addPieSlice(
            PieModel(
                "Online időpontok",
                sumOnlineHours.toFloat(),
                Color.parseColor("#FFA96C")
            )
        )

        ch1Sum.text = "${sumLocalHours.toInt() + sumOnlineHours.toInt()} óra"
        ch1Local.text = "${sumLocalHours.toInt()} óra"
        ch1Online.text = "${sumOnlineHours.toInt()} óra"
        pieChart1!!.startAnimation()
    }

    private fun setDataPieChart2(sumLocalIncome: Double, sumOnlineIncome: Double) {
        pieChart2!!.addPieSlice(
            PieModel(
                "Személyes időpontok",
                sumLocalIncome.toFloat(),
                Color.parseColor("#FF6B6B")
            )
        )
        pieChart2!!.addPieSlice(
            PieModel(
                "Online időpontok",
                sumOnlineIncome.toFloat(),
                Color.parseColor("#FFA96C")
            )
        )
        ch2Sum.text = "${sumLocalIncome.toInt() + sumOnlineIncome.toInt()} Ft"
        ch2Local.text = "${sumLocalIncome.toInt()} Ft"
        ch2Online.text = "${sumOnlineIncome.toInt()} Ft"
        pieChart2!!.startAnimation()
    }
}
