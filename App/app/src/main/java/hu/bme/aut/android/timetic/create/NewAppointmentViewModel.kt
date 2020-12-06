package hu.bme.aut.android.timetic.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.models.*
import hu.bme.aut.android.timetic.ui.loginAregistration.Result
import java.util.*

class NewAppointmentViewModel: ViewModel() {
    private lateinit var backend: NetworkOrganizationInteractor
    private var repo: DBRepository

    private lateinit var data: ForEmployeeDataForAppointmentCreation

    private val _result = MutableLiveData<Result>()
    val result: LiveData<Result> = _result

    private val _activities = MutableLiveData<List<CommonActivity>>()
    val activities: LiveData<List<CommonActivity>> = _activities

    private val _clients = MutableLiveData<List<CommonClient>>()
    val clients: LiveData<List<CommonClient>> = _clients

    private val _locations = MutableLiveData<List<String>>()
    val locations: LiveData<List<String>> = _locations

    private val _employee = MutableLiveData<CommonEmployee>()
    val employee: LiveData<CommonEmployee> = _employee

    lateinit var appDetail: LiveData<Appointment>
    lateinit var personDetail: LiveData<Person>

    private val _meetingUrl = MutableLiveData<String>()
    val meetingUrl: LiveData<String> = _meetingUrl

    private lateinit var id: String

    init{
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)
    }

    fun getAppointmentByNetId(netId: String){
        appDetail = repo.getAppointmentByNetId(netId)
    }

    fun getPersonByNetId(netId: String){
        personDetail = repo.getPersonByNetId(netId)
    }

    fun getDataForAppointmentCreation(organizationUrl: String, token: String){
        backend =
            NetworkOrganizationInteractor(organizationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = this::error)
    }

    private fun successDataForCreation(data: ForEmployeeDataForAppointmentCreation) {
        this.data = data

        _locations.value = data.places
        _clients.value = data.clients
        _activities.value = data.activities
        _employee.value = data.employees?.get(0)
    }

    private fun error(e: Throwable, code: Int?, call: String) {
        _result.value = Result(false, R.string.errorCreateAppointment)
        UseCases.logBackendError(e, code, call)
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

    fun cancelAppointmentByClient(organizationUrl: String, token: String, id: String){
        val interactor =
            NetworkOrganizationInteractor(organizationUrl,
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

    fun getMeetingUrlByClient(organizationUrl: String, token: String, appointmentId: String) {
        val interactor =
            NetworkOrganizationInteractor(organizationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        interactor.getMeetingUrlForClient(appointmentId, this::successMeetingUrl, this::error)
    }

    private fun successMeetingUrl(commonConsultation: CommonConsultation)  {
        _meetingUrl.value = commonConsultation.url
    }


    private fun successCancelAppointment(u: Unit)  {
        _result.value = Result(true, null)
    }

    private fun successModifyAppointment(appointment: CommonAppointment)  {
        _result.value = Result(true, null)
    }

    private fun successAddAppointment(appointment: CommonAppointment) {
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
            videochat = null, location = place, personBackendId = null, activity = null)
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
            location = place,
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
        location = place,
        personBackendId = employee!!.id,
        activity = activity!!.name,
        organizationUrl = url
    )
}

fun ForClientAppointment.getEmployee(): Person {
    return Person(id = null, backendId = employee!!.id!!, name = employee.name!!, email = employee.email!!, phone = employee.phone!!)
}