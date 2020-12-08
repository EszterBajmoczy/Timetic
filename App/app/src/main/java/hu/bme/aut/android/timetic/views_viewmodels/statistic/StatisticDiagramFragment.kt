package hu.bme.aut.android.timetic.views_viewmodels.statistic

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.network.models.ForEmployeeActivityForReport
import kotlinx.android.synthetic.main.statistic_diagram_detail.view.*
import kotlinx.android.synthetic.main.statistic_diagram_fragment.*
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel


class StatisticDiagramFragment : Fragment() {
    var chart1: PieChart? = null
    var chart2: PieChart? = null
    var chart3: PieChart? = null
    var chart4: PieChart? = null

    private lateinit var mInflater: LayoutInflater

    private lateinit var viewModel: StatisticDiagramViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = false

        val rootView = inflater.inflate(R.layout.statistic_diagram_fragment, container, false)
        mInflater = LayoutInflater.from(context)
        return rootView
    }

    override fun onDestroyView() {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = true
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(StatisticDiagramViewModel::class.java)

        chart1 = piechart1
        chart2 = piechart2
        chart3 = piechart3
        chart4 = piechart4

        viewModel.data.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it.activities != null && it.activities.size <= 1) {
                piechart1.visibility = View.GONE
                piechart2.visibility = View.GONE
            } else{
                setDataPieChart1(it.activities!!)
                setDataPieChart2(it.activities)
            }
            setDataPieChart3(it.sumLocalHours!!, it.sumOnlineHours!!)
            setDataPieChart4(it.sumLocalIncome!!, it.sumOnlineIncome!!)
        })
    }

    //sumhours of activities
    private fun setDataPieChart1(list: List<ForEmployeeActivityForReport>) {
        ch1Container.removeAllViewsInLayout()
        chart1!!.clearChart()
        var sum = 0

        val colors = resources.getStringArray(R.array.color_values)
        for((index, item) in list.withIndex()) {
            item.sumHours?.let {
                sum += item.sumHours.toInt()
            }

            chart1!!.addPieSlice(
                PieModel(
                    item.name,
                    item.sumHours!!.toFloat(),
                    Color.parseColor(colors[index])
                )
            )
            val itemRow = mInflater.inflate(R.layout.statistic_diagram_detail, null, false)
            itemRow.ch1Color.setBackgroundColor(Color.parseColor(colors[index]))
            itemRow.subTitle.text = item.name
            itemRow.subSum.text = "${item.sumHours.toInt()} óra"

            ch1Container.addView(itemRow)
        }
        ch1Sum.text = "${sum} óra"
        chart1!!.startAnimation()
    }

    //sumIncome of activities
    private fun setDataPieChart2(list: List<ForEmployeeActivityForReport>) {
        ch2Container.removeAllViewsInLayout()
        chart2!!.clearChart()
        var sum = 0

        val colors = resources.getStringArray(R.array.color_values)
        for((index, item) in list.withIndex()) {
            item.sumIncome?.let {
                sum += it.toInt()
            }

            chart2!!.addPieSlice(
                PieModel(
                    item.name,
                    item.sumIncome!!.toFloat(),
                    Color.parseColor(colors[index])
                )
            )
            val itemRow = mInflater.inflate(R.layout.statistic_diagram_detail, null, false)
            itemRow.ch1Color.setBackgroundColor(Color.parseColor(colors[index]))
            itemRow.subTitle.text = item.name
            itemRow.subSum.text = "${item.sumIncome.toInt()} Ft"

            ch2Container.addView(itemRow)
        }
        ch2Sum.text = "${sum} Ft"
        chart2!!.startAnimation()
    }


    private fun setDataPieChart3(sumLocalHours: Double, sumOnlineHours: Double) {
        chart3!!.clearChart()
        chart3!!.addPieSlice(
            PieModel(
                resources.getString(R.string.private_appointments)                ,
                sumLocalHours.toFloat(),
                Color.parseColor("#FF6B6B")
            )
        )
        chart3!!.addPieSlice(
            PieModel(
                resources.getString(R.string.online_appointments),
                sumOnlineHours.toFloat(),
                Color.parseColor("#FFA96C")
            )
        )

        ch3Sum.text = "${sumLocalHours.toInt() + sumOnlineHours.toInt()} óra"
        ch3Local.text = "${sumLocalHours.toInt()} óra"
        ch3Online.text = "${sumOnlineHours.toInt()} óra"
        chart3!!.startAnimation()
    }

    private fun setDataPieChart4(sumLocalIncome: Double, sumOnlineIncome: Double) {
        chart4!!.clearChart()
        chart4!!.addPieSlice(
            PieModel(
                resources.getString(R.string.private_appointments),
                sumLocalIncome.toFloat(),
                Color.parseColor("#FF6B6B")
            )
        )
        chart4!!.addPieSlice(
            PieModel(
                resources.getString(R.string.online_appointments),
                sumOnlineIncome.toFloat(),
                Color.parseColor("#FFA96C")
            )
        )
        ch4Sum.text = "${sumLocalIncome.toInt() + sumOnlineIncome.toInt()} Ft"
        ch4Local.text = "${sumLocalIncome.toInt()} Ft"
        ch4Online.text = "${sumOnlineIncome.toInt()} Ft"
        chart4!!.startAnimation()
    }
}
