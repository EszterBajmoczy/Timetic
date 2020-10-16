package hu.bme.aut.android.timetic.ui.statistic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.alamkanak.weekview.WeekView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.R
import kotlinx.android.synthetic.main.statistic_diagram_fragment.*
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel





class StatisticDiagramFragment : Fragment() {
    var tvR: TextView? = null
    var tvPython:TextView? = null
    var tvCPP:TextView? = null
    var tvJava:TextView? = null
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



        pieChart1 = piechart1
        pieChart2 = piechart2
        setDataPieChart1()
        setDataPieChart2()
    }

    fun setDataPieChart1(){
        pieChart1!!.addPieSlice(
            PieModel(
                "Személyes időpontok",
                15.toFloat(),
                Color.parseColor("#FF6B6B")

            )
        )
        pieChart1!!.addPieSlice(
            PieModel(
                "Online időpontok", 20.toFloat(),
                Color.parseColor("#FFA96C")
            )
        )
        pieChart1!!.startAnimation()
    }

    fun setDataPieChart2(){
        pieChart2!!.addPieSlice(
            PieModel(
                "Személyes időpontok",
                13.toFloat(),
                Color.parseColor("#FF6B6B")

            )
        )
        pieChart2!!.addPieSlice(
            PieModel(
                "Online időpontok", 22.toFloat(),
                Color.parseColor("#FFA96C")
            )
        )
        pieChart2!!.startAnimation()
    }
}
