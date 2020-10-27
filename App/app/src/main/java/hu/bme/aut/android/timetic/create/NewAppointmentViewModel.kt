package hu.bme.aut.android.timetic.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.models.*
import kotlinx.coroutines.launch
import java.util.*

class NewAppointmentViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor
    private var repo: DBRepository

    private lateinit var data: ForEmployeeDataForAppointmentCreation

    private val _activities = MutableLiveData<List<CommonActivity>>()
    val activities: LiveData<List<CommonActivity>> = _activities

    private val _clients = MutableLiveData<List<CommonClient>>()
    val clients: LiveData<List<CommonClient>> = _clients

    private val _places = MutableLiveData<List<String>>()
    val places: LiveData<List<String>> = _places

    private val _employee = MutableLiveData<CommonEmployee>()
    val employee: LiveData<CommonEmployee> = _employee

    var appDetail: LiveData<Appointment>

    private val _result = MutableLiveData<Boolean>()
    val result: LiveData<Boolean> = _result

    private lateinit var id: String

    init{
        val dao = MyApplication.myDatabase.roomDao()
        Log.d("EZAZ", "newapp")
        repo = DBRepository(dao)
        appDetail = repo.getAppointmentByNetId("")
    }

    fun getAppointmentByNetId(netId: String){
        appDetail = repo.getAppointmentByNetId(netId)
    }

    fun getDataForAppointmentCreation(organisationUrl: String, token: String){
        backend =
            NetworkOrganisationInteractor(
                organisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = this::errorDataForCreation)
    }

    private fun successDataForCreation(data: ForEmployeeDataForAppointmentCreation) {
        Log.d("EZAZ", "data success")

        this.data = data

        _places.value = data.places
        _clients.value = data.clients
        _activities.value = data.activities
        _employee.value = data.employees?.get(0)
    }

    private fun errorDataForCreation(e: Throwable) {
        Log.d("EZAZ", "data errrrrror")

        //TODO
    }


    fun saveAppointment(appointment: CommonAppointment){
        backend.addAppointment(appointment, onSuccess = this::successAddAppointment, onError = this::errorAddAppointment)
    }

    fun modifyAppointment(appointment: CommonAppointment){
        backend.modifyAppointment(appointment, this::successModifyAppointment, this::errorModifyAppointment)
    }

    fun cancelAppointment(id: String){

        this.id = id
        backend.cancelAppointment(id, this::successCancelAppointment, this::errorCancelAppointment)
    }

    private fun getActivity(name: String) : CommonActivity? {
        for(item in _activities.value!!){
            if(item.name == name){
                return item
            }
        }
        return null
    }

    private fun getClient(name: String) : CommonClient? {
        for(item in _clients.value!!){
            if(item.name == name){
                return item
            }
        }
        return null
    }

    private fun errorCancelAppointment(e: Throwable) {
        Log.d("EZAZ", "cancel errrrrror")
        //TODO
    }

    private fun successCancelAppointment(u: Unit)  {
        Log.d("EZAZ", "cancel success")
        Log.d("EZAZ", "id ${appDetail.value!!.netId}")

        delete()
    }

    private fun delete()  = viewModelScope.launch {
        if (appDetail.value != null) {
            repo.deleteAppointment(appDetail.value!!)
        }
    }


    private fun errorModifyAppointment(e: Throwable) {
        Log.d("EZAZ", "modify errrrrror")
        //TODO
    }

    private fun successModifyAppointment(appointment: CommonAppointment)  {
        Log.d("EZAZ", "modifying success")

        deleteById(appointment.id!!)

        val start = Calendar.getInstance()
        start.timeInMillis = appointment.startTime!!
        val end = Calendar.getInstance()
        end.timeInMillis = appointment.endTime!!

        val a = Appointment(id = null, netId = appointment.id, note = appointment.note, start_date = start, end_date = end, price = appointment.price, private_appointment = appointment.isPrivate!!,
            videochat = appointment.online!!, address = appointment.place, client = appointment.client!!.name, activity = appointment.activity!!.name)
        insert(a)

        val c = Client(id = null, netId = appointment.client.id!!, name = appointment.client.name!!, email = appointment.client.email!!, phone = appointment.client.phone!!)
        if(newOrUpdatedClient(appointment.client, c)){
            insert(c)
        }
    }

    private fun deleteById(id: String)   = viewModelScope.launch {
        repo.deleteAppointmentByNetId(id)

        _result.value = true
    }

    private fun successAddAppointment(appointment: CommonAppointment) {
        Log.d("EZAZ", "adding success")

        val start = Calendar.getInstance()
        start.timeInMillis = appointment.startTime!!
        val end = Calendar.getInstance()
        end.timeInMillis = appointment.endTime!!

        val a = Appointment(id = null, netId = appointment.id!!, note = appointment.note, start_date = start, end_date = end, price = appointment.price, private_appointment = appointment.isPrivate!!,
            videochat = appointment.online!!, address = appointment.place, client = appointment.client!!.name, activity = appointment.activity!!.name)
        insert(a)

        val c = Client(id = null, netId = appointment.client.id!!, name = appointment.client.name!!, email = appointment.client.email!!, phone = appointment.client.phone!!)
        if(newOrUpdatedClient(appointment.client, c)){
            insert(c)
        }
    }

    private fun errorAddAppointment(e: Throwable) {
        Log.d("EZAZ", "adding errrrrror")
        //TODO
    }

    //checks if the client already saved
    private fun newOrUpdatedClient(
        client: CommonClient,
        c: Client
    ) : Boolean {
        clients.let {
            for(item in clients.value!!){
                if(item.id == client.id){
                    if(item.name == client.name && item.email == client.email && item.phone == client.phone ){
                        return false
                    }
                    delete(c)
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

    private fun delete(client: Client) = viewModelScope.launch {
        repo.deleteClient(client)
    }
}