package hu.bme.aut.android.timetic.ui.clientoperations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonClient

class ClientOperationsViewModel : ViewModel() {

    private lateinit var backend: NetworkOrganizationInteractor

    val _persons = MutableLiveData<List<Person>>()
    var persons: LiveData<List<Person>> = _persons

    fun fetchData(local: Boolean,
                  OrganizationUrl: String,
                  Token: String
    ) {
        //if there isn't network connection
        if(local){
            val dao = MyApplication.myDatabase.roomDao()
            val repo = DBRepository(dao)
            persons = repo.getAllPersons()

        }
        else{
            backend =
                NetworkOrganizationInteractor(
                    OrganizationUrl,
                    null,
                    HttpBearerAuth(
                        "bearer",
                        Token
                    )
                )
            backend.getClients(onSuccess = this::success, onError = UseCases.Companion::logBackendError)
        }
    }

    private fun success(data: List<CommonClient>) {
        Log.d("EZAZ", "data client success")
        val list = ArrayList<Person>()
        for(item in data){
            val c = Person(id = null, backendId = item.id!!, name = item.name!!, email = item.email!!, phone = item.phone!!)
            list.add(c)
        }
        _persons.value = list
    }
}