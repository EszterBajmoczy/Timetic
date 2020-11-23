package hu.bme.aut.android.timetic.create

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import hu.bme.aut.android.timetic.*
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.network.models.CommonActivity
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.network.models.CommonEmployee
import kotlinx.android.synthetic.main.activity_new_appointment.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NewAppointmentActivity : AppCompatActivity() {
    private lateinit var viewModel: NewAppointmentViewModel
    private lateinit var defaultSharedPreferences: SharedPreferences
    private lateinit var role: Role
    private var startTime : Long? = null
    private var endTime: Long? = null

    private lateinit var activities : List<CommonActivity>
    private lateinit var clients : List<CommonClient>
    private lateinit var employee : CommonEmployee

    private lateinit var activity : CommonActivity
    private lateinit var client : CommonClient
    private lateinit var place : String

    private var appointment: Appointment? = null
    private lateinit var observer: Observer<Appointment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_appointment)
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences (applicationContext)
        val appointmentId = intent.getStringExtra("NetId")

        viewModel = ViewModelProviders.of(this).get(NewAppointmentViewModel::class.java)

        viewModel.result.observe(this, Observer{
            if(it.success!!) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, it.error!!, Toast.LENGTH_LONG).show()
            }
        })

        if(MyApplication.getOrganizationUrl().isNullOrEmpty()){
            role = Role.CLIENT
            //Details view
            viewModel.getAppointmentByNetId(appointmentId!!)
            viewModel.appDetail.observe(this,  clientDetailViewObserver())
        } else {
            role = Role.EMPLOYEE
            if(isNetworkAvailable()) {
                viewModel.getDataForAppointmentCreation(MyApplication.getOrganizationUrl()!!, MyApplication.getToken()!!)

                swPrivate.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked) {
                        tActivity.visibility = View.GONE
                        spActivity.visibility = View.GONE

                        tClient.visibility = View.GONE
                        spPerson.visibility = View.GONE

                        tPrice.visibility = View.GONE
                        etPrice.visibility = View.GONE
                        tFt.visibility = View.GONE

                        tVideochat.visibility = View.GONE
                        swVideochat.visibility = View.GONE
                    }
                    else {
                        tActivity.visibility = View.VISIBLE
                        spActivity.visibility = View.VISIBLE

                        tClient.visibility = View.VISIBLE
                        spPerson.visibility = View.VISIBLE

                        tPrice.visibility = View.VISIBLE
                        etPrice.visibility = View.VISIBLE
                        tFt.visibility = View.VISIBLE

                        tVideochat.visibility = View.VISIBLE
                        swVideochat.visibility = View.VISIBLE
                    }
                }

                if(appointmentId != null){
                    //Details view
                    viewModel.getAppointmentByNetId(appointmentId)
                    viewModel.appDetail.observe(this,  appObserver())
                }
                else{
                    //Create view
                    viewModel.clients.observe(this, androidx.lifecycle.Observer {
                        clients = it
                        setPersonSpinner(getStringClients(it))
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
                    clientContacts.visibility = View.GONE

                    setDateChooseButtons()

                    btCancel.setOnClickListener {
                        finish()
                    }

                    btSave.setOnClickListener {
                        if(checkData()){
                            if(isNetworkAvailable()){
                                viewModel.saveAppointment(getAppointment())
                            } else {
                                Toast.makeText(applicationContext,  getString(R.string.network_needed_new_appointment), Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    swVideochat.isChecked = defaultSharedPreferences.getBoolean("defaultVideoChat",false)

                    etPrice.setText(defaultSharedPreferences.getString("price", ""))
                }

                btConsultation.setOnClickListener {
                    if(isNetworkAvailable()){
                        viewModel.getMeetingUrl(appointmentId!!)
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.network_needed_join_meeting), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                viewModel.getAppointmentByNetId(appointmentId!!)
                viewModel.appDetail.observe(this,  clientDetailViewObserver())
            }
        }
        viewModel.meetingUrl.observe(this, Observer {
            JitsiMeetActivity.launch(this, it)
            finish()
        })
    }

    private fun appObserver(): Observer<Appointment> {
        observer = androidx.lifecycle.Observer { app ->
            appointment = app
            viewModel.clients.observe(this, androidx.lifecycle.Observer { clientList->
                clients = clientList
                app.personBackendId?.let{ clientId->
                    val person = getClient(clientList, clientId)!!
                    setPersonSpinner(getStringClients(clientList), person.name)

                    clientCall.setOnClickListener {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:${person.phone}")
                        startActivity(callIntent)
                    }

                    clientEmail.setOnClickListener {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", person.email, null))
                        startActivity(Intent.createChooser(intent, "Choose an Email client :"))
                    }
                }
            })

            viewModel.activities.observe(this, androidx.lifecycle.Observer {
                activities = it
                setActivitySpinner(getStringActivities(it), app.activity)
            })

            viewModel.places.observe(this, androidx.lifecycle.Observer {
                setLocationSpinner(it, app.address)
            })
            viewModel.employee.observe(this, androidx.lifecycle.Observer {
                employee = it
            })
            setTitle(R.string.title_activity_new_appointment_Detail)

            swPrivate.isChecked = app.private_appointment

            setDateChooseButtonValue()
            setDateChooseButtons()

            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_MONTH, 1)
            tomorrow.set(Calendar.HOUR_OF_DAY, 0)
            tomorrow.set(Calendar.MINUTE, 0)
            tomorrow.set(Calendar.MILLISECOND, 0)
            tomorrow.add(Calendar.MILLISECOND, -1)

            btCancel.setText(R.string.tCancelAppointment)

            btSave.setText(R.string.tModify)
            if(app.start_date.before(tomorrow)){
                btSave.visibility = View.GONE
                btCancel.visibility = View.GONE
            } else {
                btCancel.setOnClickListener {
                    if(isNetworkAvailable()) {
                        viewModel.appDetail.removeObserver(observer)
                        viewModel.cancelAppointment(app.backendId)
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.network_needed_cancel_appointment), Toast.LENGTH_LONG).show()
                    }
                }
                btSave.setOnClickListener {
                    if(isNetworkAvailable()) {
                        val a = getAppointment()

                        if(app.start_date.timeInMillis == a.startTime && app.end_date.timeInMillis == a.endTime &&
                            app.private_appointment == a.isPrivate && app.address == a.place && app.note == a.note &&
                            a.isPrivate){

                            finish()
                        }
                        else if (app.start_date.timeInMillis == a.startTime && app.end_date.timeInMillis == a.endTime &&
                            app.private_appointment == a.isPrivate && app.personBackendId == a.client!!.id &&
                            app.activity == a.activity!!.name && app.address == a.place &&
                            app.price == a.price && app.videochat == a.online && app.note == a.note) {

                            finish()
                        }
                        else if(checkData()){
                            viewModel.modifyAppointment(a)
                        }
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.network_needed_modify_appointment), Toast.LENGTH_LONG).show()
                    }
                }
            }

            if(app.videochat != null){
                swVideochat.isChecked = app.videochat
                val c = Calendar.getInstance()
                if(app.end_date.after(c) && app.videochat){
                    btConsultation.visibility = View.VISIBLE
                }
            }
            else{
                swVideochat.isChecked = false
            }

            etNote.setText(app.note)

            etPrice.setText(app.price.toString())
        }
        return observer
    }

    private fun clientDetailViewObserver(): Observer<Appointment> {
        observer = androidx.lifecycle.Observer { app ->
            appointment = app

            if(app.personBackendId != null && app.personBackendId.isNotEmpty()){
                viewModel.getPersonByNetId(app.personBackendId)
                viewModel.personDetail.observe(this, Observer{person ->
                    setPersonSpinner(listOf(person.name), person.name)
                    spPerson.isClickable = false


                    clientCall.setOnClickListener {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:${person.phone}")
                        startActivity(callIntent)
                    }

                    clientEmail.setOnClickListener {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", person.phone, null))
                        startActivity(Intent.createChooser(intent, "Choose an Email client :"))
                    }
                })
            }

            setActivitySpinner(listOf(app.activity), null)
            spActivity.isClickable = false
            setLocationSpinner(listOf(app.address), app.address)
            spLocation.isClickable = false

            viewModel.employee.observe(this, androidx.lifecycle.Observer {
                employee = it
            })
            setTitle(R.string.title_activity_new_appointment_Detail)

            tPrivate.visibility = View.GONE
            swPrivate.visibility = View.GONE

            etPrice.setText(app.price.toString())
            etPrice.isClickable = false

            setDateChooseButtonValue()

            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_MONTH, 1)
            tomorrow.set(Calendar.HOUR_OF_DAY, 0)
            tomorrow.set(Calendar.MINUTE, 0)
            tomorrow.set(Calendar.MILLISECOND, 0)
            tomorrow.add(Calendar.MILLISECOND, -1)

            btCancel.setText(R.string.tCancelAppointment)
            if(role == Role.EMPLOYEE){
                btSave.setText(R.string.tModify)
            } else {
                btSave.visibility = View.GONE
            }

            if(app.videochat != null){
                swVideochat.isChecked = app.videochat
                val c = Calendar.getInstance()
                if(app.end_date.after(c) && app.videochat){
                    btConsultation.visibility = View.VISIBLE

                    btConsultation.setOnClickListener {
                        if(isNetworkAvailable()){
                            val organizationsMapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
                            val organizationMap = organizationsMapString!!.toHashMap()
                            if(organizationMap.containsKey(app.organizationUrl)) {
                                viewModel.getMeetingUrlByClient(app.organizationUrl!!, organizationMap[app.organizationUrl!!]!!, app.backendId)
                            }
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.network_needed_join_meeting), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            else{
                swVideochat.isChecked = false
            }
            swVideochat.isClickable = false

            etNote.setText(app.note)
            etNote.isClickable = false

            if(app.start_date.before(tomorrow)){
                btCancel.visibility = View.GONE
            } else {
                btCancel.setOnClickListener {
                    if(isNetworkAvailable()) {
                        viewModel.appDetail.removeObserver(observer)

                        val organizationsMapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
                        val organizationMap = organizationsMapString!!.toHashMap()
                        if(organizationMap.containsKey(app.organizationUrl)){
                            viewModel.cancelAppointmentByClient(
                                app.organizationUrl!!,
                                organizationMap[app.organizationUrl!!]!!,
                                app.backendId
                            )
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.network_needed_cancel_appointment), Toast.LENGTH_LONG).show()
                    }
                }

                btSave.setOnClickListener {
                    Toast.makeText(this, getString(R.string.network_needed_cancel_appointment), Toast.LENGTH_LONG).show()
                }
            }
        }
        return observer
    }

    private fun setDateChooseButtonValue(){
        val simpleFormat = SimpleDateFormat("MM.dd.yyyy\nHH:mm", Locale.getDefault())
        if(appointment != null){
            btChooseStartTime.text = simpleFormat.format(appointment!!.start_date.time)
            startTime = appointment!!.start_date.timeInMillis
            btChooseEndTime.text = simpleFormat.format(appointment!!.end_date.time)
            endTime = appointment!!.end_date.timeInMillis
        }

    }

    private fun getAppointment() : CommonAppointment{
        return if(swPrivate.isChecked){
            CommonAppointment(id = appointment?.backendId, isPrivate = swPrivate.isChecked, startTime = startTime, endTime = endTime, client = null, activity = null, employee = employee, place = place,
                price = null, online = null, note = etNote.text.toString())
        }
        else{
            CommonAppointment(id = appointment?.backendId, isPrivate = swPrivate.isChecked, startTime = startTime, endTime = endTime, client = client, activity = activity, employee = employee, place = place,
                price = etPrice.text.toString().toDouble(), online = swVideochat.isChecked, note = etNote.text.toString())
        }
    }

    private fun checkData(): Boolean {
        if(startTime == null || endTime == null ){
            Toast.makeText(this, getString(R.string.appointment_needed), Toast.LENGTH_LONG).show()
            return false
        }
        if(startTime != null && endTime != null && startTime!! >= endTime!!){
            Toast.makeText(this, getString(R.string.appointments_error), Toast.LENGTH_LONG).show()
            return false
        }
        if(swPrivate.isChecked){
            return true
        }
        if(startTime != null && endTime != null && etPrice.text.toString() != ""){
            //it is not possible to modify and add appointment before tomorrow
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_MONTH, 1)
            tomorrow.set(Calendar.HOUR_OF_DAY, 0)
            tomorrow.set(Calendar.MINUTE, 0)
            tomorrow.set(Calendar.MILLISECOND, 0)
            tomorrow.add(Calendar.MILLISECOND, -1)

            if(startTime != null && endTime != null && startTime!! < tomorrow.timeInMillis){
                Toast.makeText(this, getString(R.string.appointment_befare_tommorow_error), Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }
        else if(startTime != null && endTime != null) {
            Toast.makeText(this, getString(R.string.price_error), Toast.LENGTH_LONG).show()
            return false
        }
        return false
    }

    private fun setDateChooseButtons(){
        btChooseStartTime.setOnClickListener {
            var c = Calendar.getInstance()
            if(appointment != null){
                c = appointment!!.start_date
            }

            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        OnTimeSetListener { _, hourOfDay, minute ->
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            c.set(Calendar.MINUTE, minute)
                            startTime = c.timeInMillis

                            val simpleFormat = SimpleDateFormat("MM.dd.yyyy\nHH:mm", Locale.getDefault())
                            btChooseStartTime.text = simpleFormat.format(c.time)

                            val defaultTimeLength = defaultSharedPreferences.getString("timeRange", "60")?.toInt()
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
            var c = Calendar.getInstance()
            c.timeInMillis = startTime ?: c.timeInMillis
            val defaultTimeLength = defaultSharedPreferences.getString("timeRange", "60")?.toInt()
            if(defaultTimeLength != null){
                c.add(Calendar.MINUTE, defaultTimeLength)
            }
            if(appointment != null) {
                c = appointment!!.end_date
            }

            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        OnTimeSetListener { _, hourOfDay, minute ->
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            c.set(Calendar.MINUTE, minute)
                            endTime = c.timeInMillis

                            val simpleFormat = SimpleDateFormat("MM.dd.yyyy\nHH:mm", Locale.getDefault())
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

    private fun setActivitySpinner(
        activityList: List<String?>,
        activity: String? = null
    ) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, activityList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spActivity.adapter = arrayAdapter

        if(activity != null){
            spActivity.setSelection(activityList.indexOf(activity))
        }
        else{
            //setDefaultActivity from settings
            val defaultActivity = defaultSharedPreferences.getString("activityType", "")
            defaultActivity?.let {
                val index = activityList.indexOf(defaultActivity)
                if(index != -1){
                    spActivity.setSelection(index)
                }
            }
        }
        if(!MyApplication.getOrganizationUrl().isNullOrEmpty() && isNetworkAvailable()){
            spActivity.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    this@NewAppointmentActivity.activity = activities[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setPersonSpinner(
        clientList: List<String?>,
        client: String? = null
    ) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, clientList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPerson.adapter = arrayAdapter
        if(client != null){
            spPerson.setSelection(clientList.indexOf(client))
        }
        if(!MyApplication.getOrganizationUrl().isNullOrEmpty() && isNetworkAvailable()){
            spPerson.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    this@NewAppointmentActivity.client = clients[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setLocationSpinner(
        placesList: List<String?>,
        address: String? = null
    ) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, placesList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLocation.adapter = arrayAdapter
        if(address != null){
            spLocation.setSelection(placesList.indexOf(address))
        }
        if(!MyApplication.getOrganizationUrl().isNullOrEmpty() && isNetworkAvailable()) {
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

    private fun getClient(list: List<CommonClient>, backendId: String) : Person? {
        for(item in list){
            if(item.id == backendId) {
                return Person(backendId = item.id, name = item.name!!, email = item.email!!, phone = item.phone!!)
            }
        }
        return null
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}


