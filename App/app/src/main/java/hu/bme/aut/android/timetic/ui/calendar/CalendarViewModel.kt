package hu.bme.aut.android.timetic.ui.calendar

import android.util.Log
import androidx.lifecycle.*
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.create.getAppointment
import hu.bme.aut.android.timetic.create.getClient
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.dataManager.DBRepository
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class CalendarViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor
    private var repo: DBRepository

    var apps: LiveData<List<Appointment>>
    var clients: LiveData<List<Client>>

    private val _appointments = MutableLiveData<List<CommonAppointment>>()
    var appointments: LiveData<List<CommonAppointment>> = _appointments

    private lateinit var organisationUrl: String

    init{
        val dao = MyApplication.myDatabase.roomDao()
        Log.d("EZAZ", "calendarvw")
        repo = DBRepository(dao)
        Log.d("EZAZ", "getallapps")

        apps = repo.getAllAppointments()
        clients = repo.getAllClients()
    }

    fun downloadAppointments(organisationUrl: String, token: String) {
        Log.d("EZAZ", "appontments downloooooooood")
        this.organisationUrl = organisationUrl
        backend = NetworkOrganisationInteractor(
                organisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )

        backend.getAppointments(onSuccess = this::successAppointmentList, onError = this::errorAppointmentList)
    }

    private fun successAppointmentList(list: List<CommonAppointment>) {
        Log.d("EZAZ", "appontments success")
        _appointments.value = list
        appointments = _appointments
        val appointmentIds = ArrayList<String>()
        val checkedClients = ArrayList<CommonClient>()

        //check if it is already in the local database
        for (item in list){
            appointmentIds.add(item.id!!)

            val start = Calendar.getInstance()
            start.timeInMillis = item.startTime!!
            val end = Calendar.getInstance()
            end.timeInMillis = item.endTime!!

            if(newOrUpdatedAppointment(item)){

                val a = item.getAppointment()
                if(!item.isPrivate!!){
                    val c = item.getClient()
                    if(c != null && newOrUpdatedClient(item.client!!) && !checkedClients.contains(item.client)){
                        checkedClients.add(item.client)
                        insert(c)
                    }
                }
                insert(a)
            }
        }
        deleteCanceledAppointments(appointmentIds)
    }

    //delete if some Appointment was deleted at the server side
    private fun deleteCanceledAppointments(ids: List<String>) {
        apps.let {
            for(item in apps.value!!){
                if(!ids.contains(item.netId)){
                    delete(item)
                }
            }
        }
    }

    //checks if the appointment already saved
    private fun newOrUpdatedAppointment(appointment: CommonAppointment) : Boolean {
        apps.let {
            for(item in apps.value!!){
                if(item.netId == appointment.id){
                    if(item.note == appointment.note && item.start_date.timeInMillis == appointment.startTime &&
                        item.end_date.timeInMillis == appointment.endTime &&
                        item.private_appointment == appointment.isPrivate &&
                        item.address == appointment.place && appointment.isPrivate){
                        return false
                    }
                    if(item.note == appointment.note && item.start_date.timeInMillis == appointment.startTime &&
                        item.end_date.timeInMillis == appointment.endTime && item.price == appointment.price &&
                        item.private_appointment == appointment.isPrivate && item.videochat == appointment.online &&
                        item.address == appointment.place && item.client == appointment.client!!.name && item.activity == appointment.activity!!.name){
                        return false
                    }
                    else if(item.note == appointment.note && item.start_date.timeInMillis == appointment.startTime &&
                        item.end_date.timeInMillis == appointment.endTime &&
                        item.private_appointment == appointment.isPrivate &&
                        item.address == appointment.place){
                        return false
                    }
                    delete(item)
                    return true
                }
            }
            return true
        }
    }

    //checks if the client already saved
    private fun newOrUpdatedClient(client: CommonClient) : Boolean {
        clients.let {
            for(item in clients.value!!){
                if(item.netId == client.id){
                    if(item.name == client.name && item.email == client.email && item.phone == client.phone ){
                        return false
                    }
                    delete(item)
                    return true
                }
            }
            return true
        }
    }

    private fun insert(appointment: Appointment) = viewModelScope.launch {
        repo.insert(appointment)
    }

    private fun insert(client: Client) = viewModelScope.launch {
        repo.insert(client)
    }

    private fun delete(appointment: Appointment) = viewModelScope.launch {
        repo.deleteAppointment(appointment)
    }

    private fun delete(client: Client) = viewModelScope.launch {
        repo.deleteClient(client)
    }

    private fun errorAppointmentList(e: Throwable) {
        Log.d("EZAZ", "appontments errrrrror")

        //TODO
    }
}