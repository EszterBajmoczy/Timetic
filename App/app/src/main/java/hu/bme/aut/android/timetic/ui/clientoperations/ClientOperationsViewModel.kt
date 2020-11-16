package hu.bme.aut.android.timetic.ui.clientoperations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonClient

class ClientOperationsViewModel : ViewModel() {

    private lateinit var backend: NetworkOrganisationInteractor

    val _persons = MutableLiveData<List<Person>>()
    var persons: LiveData<List<Person>> = _persons

    fun fetchData(local: Boolean,
        OrganisationUrl: String,
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
        Log.d("EZAZ", "data client success")
        val list = ArrayList<Person>()
        for(item in data){
            val c = Person(id = null, netId = item.id!!, name = item.name!!, email = item.email!!, phone = item.phone!!)
            list.add(c)
        }
        _persons.value = list
    }

    private fun error(e: Throwable, code: Int?, call: String) {
        when(code) {
            400 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "400 - Bad Request")
            401 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "401 - Unauthorized ")
            403 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "403 - Forbidden")
            404 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "404 - Not Found")
        }
        FirebaseCrashlytics.getInstance().setCustomKey("Call", call)
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}