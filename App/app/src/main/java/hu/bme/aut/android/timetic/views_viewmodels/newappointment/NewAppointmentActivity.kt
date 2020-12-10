package hu.bme.aut.android.timetic.views_viewmodels.newappointment

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import hu.bme.aut.android.timetic.*
import hu.bme.aut.android.timetic.views_viewmodels.newclient.toHashMap
import hu.bme.aut.android.timetic.models.Appointment
import hu.bme.aut.android.timetic.models.Person
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
    private var startTime : Long? = null
    private var endTime: Long? = null

    private lateinit var activities : List<CommonActivity>
    private lateinit var locations : List<String>
    private lateinit var clients : List<CommonClient>
    private lateinit var employee : CommonEmployee

    private lateinit var activity : CommonActivity
    private lateinit var client : CommonClient
    private lateinit var location : String

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
            //Role: Client
            //Details view
            viewModel.getAppointmentByNetId(appointmentId!!)
            viewModel.appDetail.observe(this,  clientObserver())
        } else {
            //Role: Employee
            if(isNetworkAvailable()) {
                viewModel.getDataForAppointmentCreation(MyApplication.getOrganizationUrl()!!, MyApplication.getToken()!!)

                swPrivate.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked) {
                        tActivity.visibility = View.GONE
                        spActivity.visibility = View.GONE

                        tLocation.visibility = View.GONE
                        spLocation.visibility = View.GONE

                        tPerson.visibility = View.GONE
                        spPerson.visibility = View.GONE

                        personContacts.visibility = View.GONE

                        tPrice.visibility = View.GONE
                        etPrice.visibility = View.GONE
                        tFt.visibility = View.GONE

                        tVideochat.visibility = View.GONE
                        swVideochat.visibility = View.GONE
                    }
                    else {
                        tActivity.visibility = View.VISIBLE
                        spActivity.visibility = View.VISIBLE

                        tLocation.visibility = View.VISIBLE
                        spLocation.visibility = View.VISIBLE

                        tPerson.visibility = View.VISIBLE
                        spPerson.visibility = View.VISIBLE

                        personContacts.visibility = View.VISIBLE

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
                    viewModel.appDetail.observe(this,  employeeObserver())
                }
                else{
                    //Create view
                    viewModel.clients.observe(this, androidx.lifecycle.Observer {
                        //set Person Spinner
                        clients = it
                        setSpinner(spPerson, getStringClients(it)) {
                                index -> this@NewAppointmentActivity.client = clients[index]
                        }
                    })

                    viewModel.activities.observe(this, androidx.lifecycle.Observer {
                        activities = it
                        //set Activity Spinner
                        val defaultActivity = defaultSharedPreferences.getString("activityType", "")
                        setSpinner(spActivity, getStringActivities(it), null, defaultActivity)
                        { index -> this@NewAppointmentActivity.activity = activities[index] }
                    })

                    viewModel.locations.observe(this, androidx.lifecycle.Observer {
                        locations = it
                        //setLocationSpinner
                        val defaultLocation = defaultSharedPreferences.getString("location", "")
                        setSpinner(spLocation, it, null, defaultLocation)
                            { index -> this@NewAppointmentActivity.location = locations[index] }

                    })
                    viewModel.employee.observe(this, androidx.lifecycle.Observer {
                        employee = it
                    })
                    setTitle(R.string.title_activity_new_appointment)
                    personContacts.visibility = View.GONE

                    setDateChooseButtons()

                    setButton(btCancel, null, View.OnClickListener {
                        finish()
                    })

                    setButton(btSave, null, View.OnClickListener {
                        if(checkData()){
                            if(isNetworkAvailable()){
                                viewModel.saveAppointment(getAppointment())
                            } else {
                                Toast.makeText(applicationContext,  getString(R.string.network_needed_new_appointment), Toast.LENGTH_LONG).show()
                            }
                        }
                    })

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
                viewModel.appDetail.observe(this,  privateAndNoInternetObserver())
            }
        }
        viewModel.meetingUrl.observe(this, Observer {
            JitsiMeetActivity.launch(this, it)
            finish()
        })
    }

    private fun privateAndNoInternetObserver(): Observer<Appointment> {
        observer = androidx.lifecycle.Observer { app ->
            if(app.private_appointment){
                privateAppointmentNoInternet(app)
            } else {
                employeeNoInternet(app)
            }
        }
        return observer
    }

    private fun personsAndContactsObserver(): Observer<Person> {
        return Observer { person ->
            //set Person Spinner
            setSpinner(spPerson, listOf<String?>(person.name), person.name) {
            index -> client = clients[index]
    }
            spPerson.isClickable = false

            personCall.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:${person.phone}")
                startActivity(callIntent)
            }

            personEmail.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", person.phone, null))
                startActivity(Intent.createChooser(intent, "Choose an Email client :"))
            }
        }
    }

    private fun employeeObserver(): Observer<Appointment> {
        observer = androidx.lifecycle.Observer { app ->
            appointment = app
            viewModel.clients.observe(this, androidx.lifecycle.Observer { clientList->
                clients = clientList
                app.personBackendId?.let{ clientId->
                    //set Person Spinner
                    val person = getClient(clientList, clientId)!!
                    setSpinner(spPerson, getStringClients(clientList), person.name) {
                        index -> this@NewAppointmentActivity.client = clients[index]
                    }

                    personCall.setOnClickListener {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:${person.phone}")
                        startActivity(callIntent)
                    }

                    personEmail.setOnClickListener {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", person.email, null))
                        startActivity(Intent.createChooser(intent, "Choose an Email client :"))
                    }
                }
            })

            viewModel.activities.observe(this, androidx.lifecycle.Observer {
                activities = it
                val defaultActivity = defaultSharedPreferences.getString("activityType", "")

                setSpinner(spActivity, getStringActivities(it), app.activity, defaultActivity)
                    { index -> this@NewAppointmentActivity.activity = activities[index] }
            })

            viewModel.locations.observe(this, androidx.lifecycle.Observer {
                locations = it
                val defaultLocation = defaultSharedPreferences.getString("location", "")

                setSpinner(spLocation, it, app.location, defaultLocation)
                    { index -> this@NewAppointmentActivity.location = locations[index] }
            })
            viewModel.employee.observe(this, androidx.lifecycle.Observer {
                employee = it
            })
            setTitle(R.string.title_activity_new_appointment_Detail)

            swPrivate.isChecked = app.private_appointment

            setDateChooseButtonValue()
            setDateChooseButtons()

            if(isInThePast(app.start_date)){
                setButton(btSave, null, null)
                setButton(btCancel, null, null)
            } else {
                setButton(btCancel, R.string.tCancelAppointment, View.OnClickListener {
                    if(isNetworkAvailable()) {
                        removeDetailObservers()
                        viewModel.cancelAppointment(app.backendId)
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.network_needed_cancel_appointment), Toast.LENGTH_LONG).show()
                    }
                })

                setButton(btSave, R.string.tModify, View.OnClickListener {
                    if(isNetworkAvailable()) {
                        val a = getAppointment()

                        //first check if it is modified, if yes than validate at the end
                        if(app.start_date.timeInMillis == a.startTime && app.end_date.timeInMillis == a.endTime &&
                            app.private_appointment == a.isPrivate && app.note == a.note &&
                            a.isPrivate){
                            //private appointment, not modified
                            finish()
                        }
                        else if (!a.isPrivate!! && app.start_date.timeInMillis == a.startTime && app.end_date.timeInMillis == a.endTime &&
                            app.private_appointment == a.isPrivate && app.personBackendId == a.client!!.id &&
                            app.activity == a.activity!!.name && app.location == a.place &&
                            app.price == a.price && app.videochat == a.online && app.note == a.note) {
                            //not private appointment, not modified
                            finish()
                        }
                        else if(checkData()){
                            viewModel.modifyAppointment(a)
                        }
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.network_needed_modify_appointment), Toast.LENGTH_LONG).show()
                    }
                })
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

    private fun employeeNoInternet(app: Appointment){
        appointment = app
        setTitle(R.string.title_activity_new_appointment_Detail)

        viewModel.getPersonByNetId(app.personBackendId!!)
        viewModel.personDetail.observe(this, personsAndContactsObserver())

        setSpinners(app)

        viewModel.employee.observe(this, androidx.lifecycle.Observer {
            employee = it
        })

        etPrice.setText(app.price.toString())
        etPrice.isClickable = false

        setDateChooseButtonValue()

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

        if(isInThePast(app.start_date)){
            setButton(btCancel, null, null)
            setButton(btSave, null, null)
        } else {
            setButton(btCancel, R.string.tCancelAppointment, View.OnClickListener {
                if(isNetworkAvailable()) {
                    try {
                        removeDetailObservers()
                    } catch (e: Exception) {}


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
            })
            setButton(btSave, R.string.tModify, View.OnClickListener {
                Toast.makeText(this, getString(R.string.network_needed_cancel_appointment), Toast.LENGTH_LONG).show()

            })
        }
    }

    private fun clientObserver(): Observer<Appointment> {
        observer = androidx.lifecycle.Observer { app ->
            appointment = app
            setTitle(R.string.title_activity_new_appointment_Detail)

            viewModel.getPersonByNetId(app.personBackendId!!)
            viewModel.personDetail.observe(this, personsAndContactsObserver())

            setSpinners(app)

            viewModel.employee.observe(this, androidx.lifecycle.Observer {
                employee = it
            })

            tPrivate.visibility = View.GONE
            swPrivate.visibility = View.GONE

            etPrice.setText(app.price.toString())
            etPrice.isClickable = false

            setDateChooseButtonValue()
            setButton(btSave, null, null)

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

            if(isInThePast(app.start_date)){
                setButton(btCancel, null, null)
            } else {
                setButton(btCancel, R.string.tCancelAppointment, View.OnClickListener {
                    if(isNetworkAvailable()) {
                        removeDetailObservers()

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
                })
            }
        }
        return observer
    }

    private fun privateAppointmentNoInternet(app: Appointment) {
        //setLocationSpinner
        val defaultLocation = defaultSharedPreferences.getString("location", "")
        setSpinner(spLocation, listOf(app.location), app.location, defaultLocation)
        { index -> this@NewAppointmentActivity.location = locations[index] }

        spLocation.isClickable = false

        viewModel.employee.observe(this, androidx.lifecycle.Observer {
            employee = it
        })
        setTitle(R.string.title_activity_new_appointment_Detail)

        swPrivate.isChecked = app.private_appointment
        tActivity.visibility = View.GONE
        spActivity.visibility = View.GONE
        tLocation.visibility = View.GONE
        spLocation.visibility = View.GONE
        tPerson.visibility = View.GONE
        spPerson.visibility = View.GONE
        personContacts.visibility = View.GONE
        tPrice.visibility = View.GONE
        priceLinearLayout.visibility = View.GONE
        tVideochat.visibility = View.GONE
        swVideochat.visibility = View.GONE

        setDateChooseButtonValue()

        etNote.setText(app.note)
        etNote.isClickable = false

        if(isInThePast(app.start_date)){
            setButton(btCancel, null, null)
            setButton(btSave, null, null)
        } else {
            setButton(btCancel, R.string.tCancelAppointment, View.OnClickListener {
                if(isNetworkAvailable()) {
                    removeDetailObservers()

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
            })
            setButton(btSave, R.string.tModify, View.OnClickListener {
                Toast.makeText(this, getString(R.string.network_needed_cancel_appointment), Toast.LENGTH_LONG).show()

            })
        }
    }

    private fun setDateChooseButtonValue(){
        val simpleFormat = SimpleDateFormat("yyyy.MM.dd\nHH:mm", Locale.getDefault())
        if(appointment != null){
            btChooseStartTime.text = simpleFormat.format(appointment!!.start_date.time)
            startTime = appointment!!.start_date.timeInMillis
            btChooseEndTime.text = simpleFormat.format(appointment!!.end_date.time)
            endTime = appointment!!.end_date.timeInMillis
        }
    }

    private fun getAppointment() : CommonAppointment{
        return if(swPrivate.isChecked){
            CommonAppointment(id = appointment?.backendId, isPrivate = swPrivate.isChecked, startTime = startTime, endTime = endTime, client = null, activity = null, employee = employee, place = null,
                price = null, online = null, note = etNote.text.toString())
        }
        else{
            CommonAppointment(id = appointment?.backendId, isPrivate = swPrivate.isChecked, startTime = startTime, endTime = endTime, client = client, activity = activity, employee = employee, place = location,
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
            val date = Calendar.getInstance()
            date.timeInMillis = startTime as Long

            if(startTime != null && endTime != null && isInThePast(date)){
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

                            val simpleFormat = SimpleDateFormat("yyyy.MM.dd\nHH:mm", Locale.getDefault())
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

                            val simpleFormat = SimpleDateFormat("yyyy.MM.dd\nHH:mm", Locale.getDefault())
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

    private fun setSpinner(
        spinner: Spinner,
        list: List<String?>,
        selectedItem: String? = null,
        default: String? = null,
        saveValue: (Int) -> Unit
    ) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
        if(selectedItem != null){
            spinner.setSelection(list.indexOf(selectedItem))
        } else{
            //setDefaultLocation from settings
            default?.let {value ->
                val index = list.indexOf(value)
                if(index != -1){
                    spinner .setSelection(index)
                }
            }
        }
        if(!MyApplication.getOrganizationUrl().isNullOrEmpty() && isNetworkAvailable()) {
            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    saveValue(position)
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

    //set button and visibility
    private fun setButton(button: Button, text: Int?, listener: View.OnClickListener?) {
        if(text == null && listener == null) {
            button.visibility = View.GONE
        } else {
            if (text != null) {
                button.setText(text)
            }
            button.setOnClickListener(listener)
        }
    }

    private fun isInThePast(startDate: Calendar): Boolean {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        tomorrow.set(Calendar.HOUR_OF_DAY, 0)
        tomorrow.set(Calendar.MINUTE, 0)
        tomorrow.set(Calendar.MILLISECOND, 0)
        return startDate.before(tomorrow)
    }

    private fun setSpinners(app: Appointment) {
        //set Activity Spinner
        val defaultActivity = defaultSharedPreferences.getString("activityType", "")
        setSpinner(spActivity, listOf(app.activity), null, defaultActivity)
        { index -> this@NewAppointmentActivity.activity = activities[index] }
        spActivity.isClickable = false

        //set Location Spinner
        val defaultLocation = defaultSharedPreferences.getString("location", "")
        setSpinner(spLocation, listOf(app.location), app.location, defaultLocation)
        { index -> this@NewAppointmentActivity.location = locations[index] }
        spLocation.isClickable = false
    }

    private fun removeDetailObservers(){
        //before cancel it is important
        try {
            viewModel.appDetail.removeObservers(this)
            viewModel.personDetail.removeObservers(this)
        } catch (e: Exception) {}
    }

    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.deleteAllFromProject()

            val secureSharedPreferences = MyApplication.secureSharedPreferences

            val editor = secureSharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@NewAppointmentActivity, StartScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(logoutReceiver, IntentFilter("Logout"))
    }

    override fun onPause() {
        super.onPause()
        removeDetailObservers()
        try {
            unregisterReceiver(logoutReceiver)
        } catch (e: Exception){}
    }
}


