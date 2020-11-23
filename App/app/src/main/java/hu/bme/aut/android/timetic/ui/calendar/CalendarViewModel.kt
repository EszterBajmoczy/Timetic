package hu.bme.aut.android.timetic.ui.calendar

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
        val backendList = appsFromBackend.value
        val dbList = apps.value
        var list = dbList ?: ArrayList()

        if(dbList != null) {
            if(backendList != null) {
                //check if it is already in the local database
                Singleton.appointmentOrganizer(repo, viewModelScope, backendList, dbList)
                //result.removeSource(apps)

            } else {
                list = dbList
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
        var list = dbList ?: ArrayList()

        if(dbList != null) {
            //check if it is already in the local database
            if (backendList != null) {
                list = Singleton.personOrganizer(repo, viewModelScope, backendList, dbList)
                //clientResult.removeSource(clients)
            } else {
                list = dbList
            }
        }
        return list
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

    fun deleteAllFromProject() = viewModelScope.launch {
        repo.deleteAllTables()
    }
}