package hu.bme.aut.android.timetic.views_viewmodels.calendar

import androidx.lifecycle.*
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.Role
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.views_viewmodels.newappointment.getAppointment
import hu.bme.aut.android.timetic.views_viewmodels.newappointment.getClient
import hu.bme.aut.android.timetic.views_viewmodels.newappointment.getEmployee
import hu.bme.aut.android.timetic.connectionmanager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.models.Appointment
import hu.bme.aut.android.timetic.models.Person
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.repository.DBRepository
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
        val backendList = appsFromBackend.value
        val dbList = apps.value

        if(dbList != null) {
            if(backendList != null) {
                //check if it is already in the local database
                UseCases().appointmentOrganizer(repo, viewModelScope, backendList, dbList)
                _appsFromBackend.value = null
            }
        }
        return dbList
    }

    private fun mergeClients(
        clients: LiveData<List<Person>>,
        clientsFromBackend: LiveData<List<Person>>
    ): List<Person>? {
        val backendList = clientsFromBackend.value
        val dbList = clients.value

        if(dbList != null) {
            //check if it is already in the local database
            if (backendList != null) {
                UseCases().personOrganizer(repo, viewModelScope, backendList, dbList)
                _clientsFromBackend.value = null
            }

        }
        return dbList
    }

    fun downloadAppointments(
        role: Role,
        organizationUrl: String,
        token: String
    ) {
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
            Role.EMPLOYEE -> backend.getEmployeeAppointments(onSuccess = this::successEmployeeAppointmentList, onError = UseCases.Companion::logBackendError)
            Role.CLIENT -> backend.getClientAppointments(onSuccess = this::successClientAppointmentList, onError = UseCases.Companion::logBackendError)
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

    fun deleteAllFromProject() = viewModelScope.launch {
        repo.deleteAllTables()
    }
}