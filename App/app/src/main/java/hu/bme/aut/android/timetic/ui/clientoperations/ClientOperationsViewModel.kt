package hu.bme.aut.android.timetic.ui.clientoperations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.network.models.ForEmployeeReport

class ClientOperationsViewModel : ViewModel() {

    private lateinit var backend: NetworkOrganisationInteractor

    private val _clients = MutableLiveData<List<Client>>()
    var clients: LiveData<List<Client>> = _clients

    fun fetchData(local: Boolean,
        OrganisationUrl: String,
        Token: String
    ) {
        //if there isn't network connection
        if(local){
            val dao = MyApplication.myDatabase.roomDao()
            val repo = DBRepository(dao)
            clients = repo.getAllClients()

        }
        else{
            backend =
                NetworkOrganisationInteractor(
                    OrganisationUrl,
                    null,
                    HttpBearerAuth(
                        "bearer",
                        Token
                    )
                )
            backend.getClients(onSuccess = this::success, onError = this::error)
        }
    }

    private fun success(data: List<CommonClient>) {
        Log.d("EZAZ", "data success")
        val list = ArrayList<Client>()
        for(item in data){
            val c = Client(id = null, netId = item.id!!, name = item.name!!, email = item.email!!, phone = item.phone!!)
            list.add(c)
        }
        _clients.value = list
    }

    private fun error(e: Throwable) {
        Log.d("EZAZ", "data errrrrror")

        //TODO
    }
}