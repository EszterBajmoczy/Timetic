package hu.bme.aut.android.timetic.create

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.network.models.CommonActivity
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.network.models.CommonEmployee
import kotlinx.android.synthetic.main.activity_new_appointment.*
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class NewAppointmentActivity : AppCompatActivity() {
    private lateinit var viewModel: NewAppointmentViewModel
    private var startTime : Long? = null
    private var endTime: Long? = null

    private lateinit var activities : List<CommonActivity>
    private lateinit var clients : List<CommonClient>
    private lateinit var employee : CommonEmployee

    private lateinit var activity : CommonActivity
    private lateinit var client : CommonClient
    private lateinit var place : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_appointment)

        //TODO csak netkapcsolatkor lehessen újat létrehozni

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "secure_shared_preferences",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        //TODO viewmodelfactory?
        viewModel = NewAppointmentViewModel()
        viewModel.getDataForAppointmentCreation(secureSharedPreferences.getString("OrganisationUrl", "").toString(),
            secureSharedPreferences.getString("Token", "").toString())

        viewModel.clients.observe(this, androidx.lifecycle.Observer {
            clients = it
            setClientSpinner(getStringClients(it))
        })

        viewModel.activities.observe(this, androidx.lifecycle.Observer {
            activities = it
            setActivitySpinner(getStringActivities(it))
        })

        viewModel.places.observe(this, androidx.lifecycle.Observer {
            setLocationSpinner(it)
        })
        viewModel.employee.observe(this, androidx.lifecycle.Observer {
            employee = it
        })
        setTitle(R.string.title_activity_new_appointment)

        setNotificationSpinner()
        setDateChooseButtons()

        btCancel.setOnClickListener {
            finish()
        }

        btSave.setOnClickListener {
            if(checkData()){
                saveAppointment()
                finish()
            }

        }

        val sp = PreferenceManager.getDefaultSharedPreferences (applicationContext)
        swVideochat.isChecked = sp.getBoolean("defaultVideoChat",false)
        //TODO
        val defaultTimeLength = sp.getString("timeRange", "60")?.toInt()

        etPrice.setText(sp.getString("price", ""))
        //TODO notifications
    }

    private fun saveAppointment(){
        val a = CommonAppointment(id = null, isPrivate = swPrivate.isChecked, startTime = startTime, endTime = endTime, client = client, activity = activity, employee = employee, place = place,
            price = etPrice.text.toString().toDouble(), online = swVideochat.isChecked, note = etNote.text.toString())
        viewModel.saveAppointment(a)
    }

    private fun checkData(): Boolean {
        if(startTime != null && endTime != null && etPrice.text.toString() != ""){
            return true
        }
        else if(startTime != null && endTime != null) {
            Toast.makeText(this, "Árat kötelező megadni", Toast.LENGTH_LONG).show()
            return false
        }
        else{
            Toast.makeText(this, "Időpontot kötelező megadni", Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun setDateChooseButtons(){
        //TODO
        btChooseStartTime.setOnClickListener {
            val c = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        OnTimeSetListener { view, hourOfDay, minute ->
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            c.set(Calendar.MINUTE, minute)
                            startTime = c.timeInMillis

                            val simpleFormat = SimpleDateFormat("MM.dd.yyyy\nhh:mm", Locale.getDefault())
                            btChooseStartTime.text = simpleFormat.format(c.time)

                            val sp = PreferenceManager.getDefaultSharedPreferences (applicationContext)
                            val defaultTimeLength = sp.getString("timeRange", "60")?.toInt()
                            if (defaultTimeLength != null) {
                                c.add(Calendar.MINUTE, defaultTimeLength)
                                endTime = c.timeInMillis

                                btChooseEndTime.text = simpleFormat.format(c.time)
                            }

                        },
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btChooseEndTime.setOnClickListener {
            val c = Calendar.getInstance()
            c.timeInMillis = startTime ?: c.timeInMillis
            val sp = PreferenceManager.getDefaultSharedPreferences (applicationContext)
            val defaultTimeLength = sp.getString("timeRange", "60")?.toInt()
            if(defaultTimeLength != null){
                c.add(Calendar.MINUTE, defaultTimeLength)
            }

            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        OnTimeSetListener { view, hourOfDay, minute ->
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            c.set(Calendar.MINUTE, minute)
                            endTime = c.timeInMillis

                            val simpleFormat = SimpleDateFormat("MM.dd.yyyy\nhh:mm", Locale.getDefault())
                            btChooseEndTime.text = simpleFormat.format(c.time)
                        },
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun setActivitySpinner(activityList: List<String>) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, activityList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spActivity.adapter = arrayAdapter
        //setDefaultActivity from settigns
        val sp = PreferenceManager.getDefaultSharedPreferences (applicationContext)
        val defaultIndex = sp.getString("activityType", "0")
        if(defaultIndex != null){
            spActivity.setSelection(defaultIndex!!.toInt())
        }
        spActivity.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                activity = activities[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setClientSpinner(clientList: List<String>) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, clientList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spClient.adapter = arrayAdapter
        spClient.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                client = clients[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setLocationSpinner(placesList: List<String>) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, placesList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLocation.adapter = arrayAdapter
        spLocation.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                place = selected
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setNotificationSpinner(){
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add("1 óra")
        arrayList.add("fél óra")
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNotification.adapter = arrayAdapter
        spNotification.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getStringActivities(list: List<CommonActivity>) : List<String> {
        val stringList = ArrayList<String>()
        for(item in list){
            stringList.add(item.name!!)
        }
        return stringList
    }

    private fun getStringClients(list: List<CommonClient>) : List<String>{
        val stringList = ArrayList<String>()
        for(item in list){
            stringList.add(item.name!!)
        }
        return stringList
    }
}


