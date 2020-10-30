package hu.bme.aut.android.timetic.ui.calendar

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.models.ForEmployeeDataForAppointmentCreation
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

    init{
        val dao = MyApplication.myDatabase.roomDao()
        Log.d("EZAZ", "calendarvw")
        repo = DBRepository(dao)
        Log.d("EZAZ", "getallapps")

        apps = repo.getAllAppointments()
        clients = repo.getAllClients()
    }

    fun downloadAppointments(organisationUrl: String, token: String) {
        backend =
            NetworkOrganisationInteractor(
                organisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )

        backend.getAppointments(onSuccess = this::successAppointmentList, onError = this::errorAppointmentList)

    }

    fun addAppointment(){
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = this::errorDataForCreation)

    }

    private fun successAppointmentList(list: List<CommonAppointment>) {
        Log.d("EZAZ", "appontments success")
        _appointments.value = list
        appointments = _appointments
        val appointmentIds = ArrayList<String>()

        //check if it is already in the local database
        for (item in list){
            appointmentIds.add(item.id!!)

            val start = Calendar.getInstance()
            start.timeInMillis = item.startTime!!
            val end = Calendar.getInstance()
            end.timeInMillis = item.endTime!!

            if(newOrUpdatedAppointment(item)){
                val a = Appointment(id = null, netId = item.id!!, note = item.note, start_date = start, end_date = end, price = item.price, private_appointment = item.isPrivate!!,
                    videochat = item.online!!, address = item.place, client = item.client!!.name, activity = item.activity!!.name)
                insert(a)

                if(newOrUpdatedClient(item.client)){
                    val c = Client(id = null, netId = item.client.id!!, name = item.client.name!!, email = item.client.email!!, phone = item.client.phone!!)
                    insert(c)
                }
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
                        item.end_date.timeInMillis == appointment.endTime && item.price == appointment.price &&
                        item.private_appointment == appointment.isPrivate && item.videochat == appointment.online &&
                        item.address == appointment.place && item.client == appointment.client!!.name && item.activity == appointment.activity!!.name){
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

    private fun successDataForCreation(data: ForEmployeeDataForAppointmentCreation) {
        Log.d("EZAZ", "data success")
        //backend.addAppointment(a, onSuccess = this::successAppointment, onError = this::errorAppointment)

    }

    private fun errorDataForCreation(e: Throwable) {
        Log.d("EZAZ", "data errrrrror")

        //TODO
    }

    private fun successAppointment(token: CommonAppointment) {
        Log.d("EZAZ", "appointments success")

    }

    private fun errorAppointment(e: Throwable) {
        Log.d("EZAZ", "appointments errrrrror")

        //TODO
    }
}