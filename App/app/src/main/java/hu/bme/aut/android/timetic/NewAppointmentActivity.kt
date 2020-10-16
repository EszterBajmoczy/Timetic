package hu.bme.aut.android.timetic

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import kotlinx.android.synthetic.main.activity_new_appointment.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NewAppointmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_appointment)

        title = "Új időpont létrehozása"
        setAtivitySpinner()
        setClientSpinner()
        setLocationSpinner()
        setNotificationSpinner()
        setDateChooseButtons()
    }

    private fun setDateChooseButtons(){
        btChooseStartTime.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val now = Calendar.getInstance()
            builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
            val picker = builder.build()
            picker.show(this.supportFragmentManager, picker.toString())

            picker.addOnNegativeButtonClickListener {

            }
            picker.addOnPositiveButtonClickListener { selection ->
                val timeZoneUTC = TimeZone.getDefault()
                val offsetFromUTC = timeZoneUTC.getOffset(Date().time) * -1
                val simpleFormat = SimpleDateFormat("MM.dd.yyyy\nhh:mm", Locale.US)
                val firstDate = Date(selection.first!! + offsetFromUTC)
                val secondDate = Date(selection.second!! + offsetFromUTC)

                btChooseStartTime.text = simpleFormat.format(firstDate)
                btChooseEndTime.text = simpleFormat.format(secondDate)
            }

            picker.addOnPositiveButtonClickListener { Toast.makeText(applicationContext, "weekchanged: ", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setAtivitySpinner(){
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add("Gyógytorna")
        arrayList.add("Angol óra")
        arrayList.add("Fitnesz edzés")
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spActivity.setAdapter(arrayAdapter)
        spActivity.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                Toast.makeText(parent.context, "Selected: $selected", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun setClientSpinner(){
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add("Nagy András")
        arrayList.add("Horvát Ágnes")
        arrayList.add("Poll Gabriella")
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spClient.setAdapter(arrayAdapter)
        spClient.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                Toast.makeText(parent.context, "Selected: $selected", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun setLocationSpinner(){
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add("Nagy András")
        arrayList.add("Horvát Ágnes")
        arrayList.add("Poll Gabriella")
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLocation.setAdapter(arrayAdapter)
        spLocation.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                Toast.makeText(parent.context, "Selected: $selected", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun setNotificationSpinner(){
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add("1 óra")
        arrayList.add("fél óra")
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNotification.setAdapter(arrayAdapter)
        spNotification.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                Toast.makeText(parent.context, "Selected: $selected", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }
}


