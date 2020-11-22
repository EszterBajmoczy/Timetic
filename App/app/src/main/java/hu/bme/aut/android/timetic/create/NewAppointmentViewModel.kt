package hu.bme.aut.android.timetic.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.models.*
import hu.bme.aut.android.timetic.ui.loginAregistration.Result
import java.util.*

class NewAppointmentViewModel: ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor
    private var repo: DBRepository

    private lateinit var data: ForEmployeeDataForAppointmentCreation

    private val _result = MutableLiveData<Result>()
    val result: LiveData<Result> = _result

    private val _activities = MutableLiveData<List<CommonActivity>>()
    val activities: LiveData<List<CommonActivity>> = _activities

    private val _clients = MutableLiveData<List<CommonClient>>()
    val clients: LiveData<List<CommonClient>> = _clients

    private val _places = MutableLiveData<List<String>>()
    val places: LiveData<List<String>> = _places

    private val _employee = MutableLiveData<CommonEmployee>()
    val employee: LiveData<CommonEmployee> = _employee

    lateinit var appDetail: LiveData<Appointment>
    lateinit var personDetail: LiveData<Person>

    private val _meetingUrl = MutableLiveData<String>()
    val meetingUrl: LiveData<String> = _meetingUrl

    private lateinit var id: String

    init{
        val dao = MyApplication.myDatabase.roomDao()
        Log.d("EZAZ", "newapp")
        repo = DBRepository(dao)
    }

    fun getAppointmentByNetId(netId: String){
        appDetail = repo.getAppointmentByNetId(netId)
    }

    fun getPersonByNetId(netId: String){
        personDetail = repo.getPersonByNetId(netId)
    }

    fun getDataForAppointmentCreation(organisationUrl: String, token: String){
        backend =
            NetworkOrganisationInteractor(organisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = this::error)
    }

    private fun successDataForCreation(data: ForEmployeeDataForAppointmentCreation) {
        Log.d("EZAZ", "data success")

        this.data = data

        _places.value = data.places
        _clients.value = data.clients
        _activities.value = data.activities
        _employee.value = data.employees?.get(0)
    }

    private fun error(e: Throwable, code: Int?, call: String) {
        _result.value = Result(false, R.string.errorCreateAppointment)

        when(code) {
            400 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "400 - Bad Request")
            401 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "401 - Unauthorized ")
            403 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "403 - Forbidden")
            404 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "404 - Not Found")
            409 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "409 - Conflict")
        }
        FirebaseCrashlytics.getInstance().setCustomKey("Call", call)
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun saveAppointment(appointment: CommonAppointment){
        backend.addAppointment(appointment, onSuccess = this::successAddAppointment, onError = this::error)
    }

    fun modifyAppointment(appointment: CommonAppointment){
        backend.modifyAppointment(appointment, this::successModifyAppointment, this::error)
    }

    fun cancelAppointment(id: String){
        this.id = id
        backend.cancelAppointmentByEmployee(id, onSuccess = this::successCancelAppointment, onError = this::error)
    }

    fun cancelAppointmentByClient(organisationUrl: String, token: String, id: String){
        val interactor =
            NetworkOrganisationInteractor(organisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        interactor.cancelAppointmentByClient(id, onSuccess = this::successCancelAppointment, onError = this::error)
    }

    fun getMeetingUrl(appointmentId: String) {
        backend.getMeetingUrl(appointmentId, this::successMeetingUrl, this::error)
    }

    fun getMeetingUrlByClient(organisationUrl: String, token: String, appointmentId: String) {
        val interactor =
            NetworkOrganisationInteractor(organisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        interactor.getMeetingUrlForClient(appointmentId, this::successMeetingUrl, this::error)
    }

    private fun successMeetingUrl(commonConsultation: CommonConsultation)  {
        Log.d("EZAZ", "url success")
        _meetingUrl.value = commonConsultation.url
    }


    private fun successCancelAppointment(u: Unit)  {
        _result.value = Result(true, null)
    }

    private fun successModifyAppointment(appointment: CommonAppointment)  {
        Log.d("EZAZ", "modifying success")
        _result.value = Result(true, null)
    }

    private fun successAddAppointment(appointment: CommonAppointment) {
        Log.d("EZAZ", "adding success")
        _result.value = Result(true, null)
    }
}

fun CommonAppointment.getAppointment(): Appointment{
    val start = Calendar.getInstance()
    start.timeInMillis = startTime!!
    val end = Calendar.getInstance()
    end.timeInMillis = endTime!!
    return if(isPrivate!!){
        Appointment(id = null, backendId = id!!, note = note, start_date = start, end_date = end, price = null, private_appointment = isPrivate,
            videochat = null, address = place, personBackendId = null, activity = null)
    }
    else {
        Appointment(
            id = null,
            backendId = id!!,
            note = note,
            start_date = start,
            end_date = end,
            price = price,
            private_appointment = isPrivate,
            videochat = online!!,
            address = place,
            personBackendId = client!!.id,
            activity = activity!!.name
        )
    }
}

fun CommonAppointment.getClient(): Person? {
    return if(isPrivate!!) {
         null
    }
    else {
        Person(id = null, backendId = client!!.id!!, name = client.name!!, email = client.email!!, phone = client.phone!!)
    }
}

fun ForClientAppointment.getAppointment(url: String): Appointment{
    val start = Calendar.getInstance()
    start.timeInMillis = startTime!!
    val end = Calendar.getInstance()
    end.timeInMillis = endTime!!
    return Appointment(
        id = null,
        backendId = id!!,
        note = note,
        start_date = start,
        end_date = end,
        price = price,
        private_appointment = false,
        videochat = online!!,
        address = place,
        personBackendId = employee!!.id,
        activity = activity!!.name,
        organisationUrl = url
    )
}

fun ForClientAppointment.getEmployee(): Person {
    return Person(id = null, backendId = employee!!.id!!, name = employee.name!!, email = employee.email!!, phone = employee.phone!!)
}