package hu.bme.aut.android.timetic.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.network.models.ForEmployeeOrganization
import kotlinx.coroutines.launch


class NewClientViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor
    private lateinit var repo: DBRepository

    private val _data = MutableLiveData<ForEmployeeOrganization>()
    val data: LiveData<ForEmployeeOrganization> = _data

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    init {
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)

        backend =
            NetworkOrganisationInteractor(
                MyApplication.getOrganisationUrl()!!,
                null,
                HttpBearerAuth(
                    "bearer",
                    MyApplication.getToken()!!
                )
            )
        backend.getOrganisationData(onSuccess = this::onSuccess, onError = this::onError)
    }

    fun onSuccess(data: ForEmployeeOrganization){
        _data.value = data
    }

    fun onError(e: Throwable){
        //TODO
    }

    fun addClient(c: CommonClient) {
        backend.addClient(client = c, onSuccess = this::onSuccessClientAdd, onError = this::onErrorClientAdd)
    }

    private fun onSuccessClientAdd(client: CommonClient){
        Log.d("EZAZ", "add clienttttttttttt")
        _success.value = true
        val c = Client(netId = client.id!!, name = client.name!!, email = client.email!!, phone = client.phone!!)

        insert(c)
    }

    private fun onErrorClientAdd(e: Throwable){
        Log.d("EZAZ", "add clienttttttttttt fail")

        _success.value = false
        //TODO
    }

    private fun insert(client: Client) = viewModelScope.launch {
        repo.insert(client)
    }
}