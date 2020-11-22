package hu.bme.aut.android.timetic.ui.calendar

import android.util.Log
import androidx.lifecycle.*
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.Role
import hu.bme.aut.android.timetic.Singleton
import hu.bme.aut.android.timetic.create.getAppointment
import hu.bme.aut.android.timetic.create.getClient
import hu.bme.aut.android.timetic.create.getEmployee
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.models.ForClientAppointment
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class CalendarViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganizationInteractor
    private var repo: DBRepository

    var apps: LiveData<List<Appointment>>
    var clients: LiveData<List<Person>>

    private val _appsFromBackend = MutableLiveData<List<Appointment>>()
    var appsFromBackend: LiveData<List<Appointment>> = _appsFromBackend

    private val _clientsFromBackend = MutableLiveData<List<Person>>()
    var clientsFromBackend: LiveData<List<Person>> = _clientsFromBackend

    val result = MediatorLiveData<List<Appointment>>()
    val clientResult = MediatorLiveData<List<Person>>()

    private val _appsDownloaded = MutableLiveData<Boolean>()
    var appsDownloaded: LiveData<Boolean> = _appsDownloaded

    private lateinit var organizationUrl: String

    init{
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)

        apps = repo.getAllAppointments()
        clients = repo.getAllPersons()

        result.addSource(apps) { _ ->
            result.value = mergeAppointments(apps, appsFromBackend)
        }
        result.addSource(appsFromBackend) { _ ->
            result.value = mergeAppointments(apps, appsFromBackend)
        }

        clientResult.addSource(clients) { _ ->
            clientResult.value = mergeClients(clients, clientsFromBackend)
        }

        clientResult.addSource(clientsFromBackend) { _ ->
            clientResult.value = mergeClients(clients, clientsFromBackend)
        }
    }

    private fun mergeAppointments(
        apps: LiveData<List<Appointment>>,
        appsFromBackend: LiveData<List<Appointment>>
    ): List<Appointment>? {
        val appointmentIds = ArrayList<String>()

        val backendList = appsFromBackend.value
        val dbList = apps.value

        if(dbList != null) {
            //check if it is already in the local database
            if (backendList != null) {
                for (item in backendList){
                    appointmentIds.add(item.backendId+item.organizationUrl)
                    if(newOrUpdatedAppointment(item)){
                        insert(item)
                    }
                }
                deleteCanceledAppointments(appointmentIds)
            }
        }
        return dbList
    }

    private fun mergeClients(
        clients: LiveData<List<Person>>,
        clientsFromBackend: LiveData<List<Person>>
    ): List<Person>? {
        val clientIds = ArrayList<String>()
        val clientAlreadyAdded = ArrayList<String>()

        val backendList = clientsFromBackend.value
        val dbList = clients.value

        if(dbList != null) {
            //check if it is already in the local database
            if (backendList != null) {
                for (item in backendList){
                    clientIds.add(item.backendId+item.email)
                    if(!clientAlreadyAdded.contains(item.backendId+item.email) && newOrUpdatedClient(item) ){
                        clientAlreadyAdded.add(item.backendId+item.email)
                        insert(item)
                    }
                }
                deleteClientsWithoutAppointment(clientIds)
            }
        }
        return dbList
    }

    fun downloadAppointments(
        role: Role,
        organizationUrl: String,
        token: String
    ) {
        Log.d("EZAZ", "appontments downloooooooood")
        this.organizationUrl = organizationUrl
        backend = NetworkOrganizationInteractor(
                organizationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        when(role) {
            Role.EMPLOYEE -> backend.getEmployeeAppointments(onSuccess = this::successEmployeeAppointmentList, onError = Singleton::logBackendError)
            Role.CLIENT -> backend.getClientAppointments(onSuccess = this::successClientAppointmentList, onError = Singleton::logBackendError)
        }
    }

    private fun successEmployeeAppointmentList(list: List<CommonAppointment>) {
        _appsDownloaded.value = true

        val appList = ArrayList<Appointment>()
        val clientList = ArrayList<Person>()
        for(item in list) {
            appList.add(item.getAppointment())
            if (!item.isPrivate!! && !clientList.contains(item.getClient())){
                clientList.add(item.getClient()!!)
            }
        }
        _appsFromBackend.value = appList
        _clientsFromBackend.value = clientList
    }

    private fun successClientAppointmentList(list: List<ForClientAppointment>, organizationUrl: String) {
        _appsDownloaded.value = true

        val appList = ArrayList<Appointment>()
        val employeeList = ArrayList<Person>()
        for(item in list) {
            appList.add(item.getAppointment(organizationUrl))
            if (!employeeList.contains(item.getEmployee())){
                employeeList.add(item.getEmployee())
            }
        }
        _appsFromBackend.value = appList
        _clientsFromBackend.value = employeeList
    }

    //delete if some Appointment was deleted at the server side
    private fun deleteCanceledAppointments(ids: List<String>) {
        for(item in apps.value!!){
            if(!ids.contains(item.backendId + item.organizationUrl)){
                delete(item)
            }
        }
    }

    //checks if the appointment already saved
    private fun newOrUpdatedAppointment(
        appointment: Appointment
    ) : Boolean {
        for(item in apps.value!!){
            if(item.backendId == appointment.backendId){
                if(item.note == appointment.note && item.start_date.timeInMillis == appointment.start_date.timeInMillis &&
                    item.end_date.timeInMillis == appointment.end_date.timeInMillis &&
                    item.private_appointment == appointment.private_appointment &&
                    item.address == appointment.address && appointment.private_appointment){
                    return false
                }
                if(item.note == appointment.note && item.start_date.timeInMillis == appointment.start_date.timeInMillis &&
                    item.end_date.timeInMillis == appointment.end_date.timeInMillis && item.price!! == appointment.price &&
                    item.private_appointment == appointment.private_appointment && item.videochat == appointment.videochat &&
                    item.address == appointment.address && item.personBackendId == appointment.personBackendId && item.activity == appointment.activity){
                    return false
                }
                delete(item)
                return true
            }
        }
        return true
    }

    //delete if some Client does not have any appointment
    private fun deleteClientsWithoutAppointment(ids: List<String>) {
        for(item in clients.value!!){
            if(!ids.contains(item.backendId + item.email)){
                delete(item)
            }
        }
    }

    //checks if the client already saved
    private fun newOrUpdatedClient(
        person: Person?
    ) : Boolean {
        for(item in clients.value!!){
            if(item.backendId == person!!.backendId){
                if(item.name == person.name && item.email == person.email && item.phone == person.phone ){
                    return false
                }
                delete(item)
                return true
            }
        }
        return true
    }

    private fun insert(appointment: Appointment) = viewModelScope.launch {
        repo.insert(appointment)
    }

    private fun insert(person: Person) = viewModelScope.launch {
        repo.insert(person)
    }

    private fun delete(appointment: Appointment) = viewModelScope.launch {
        repo.deleteAppointment(appointment)
    }

    private fun delete(person: Person) = viewModelScope.launch {
        repo.deletePerson(person)
    }

    fun deleteAllFromProject() = viewModelScope.launch {
        repo.deleteAllTables()
    }
}