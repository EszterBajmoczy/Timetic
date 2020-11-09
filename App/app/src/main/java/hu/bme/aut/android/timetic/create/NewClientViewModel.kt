package hu.bme.aut.android.timetic.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonClient
import hu.bme.aut.android.timetic.network.models.ForEmployeeOrganization
import kotlinx.coroutines.launch


class NewClientViewModel : ViewModel() {
    private var backend: NetworkOrganisationInteractor
    private var repo: DBRepository

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
        backend.getOrganisationData(onSuccess = this::onSuccess, onError = this::error)
    }

    fun onSuccess(data: ForEmployeeOrganization){
        _data.value = data
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

    fun addClient(c: CommonClient) {
        backend.addClient(client = c, onSuccess = this::onSuccessClientAdd, onError = this::onErrorClientAdd)
    }

    private fun onSuccessClientAdd(client: CommonClient){
        Log.d("EZAZ", "add clienttttttttttt")
        _success.value = true
        val c = Client(netId = client.id!!, name = client.name!!, email = client.email!!, phone = client.phone!!)

        insert(c)
    }

    private fun onErrorClientAdd(e: Throwable, code: Int?, call: String){
        _success.value = false
        error(e, code, call)
    }

    private fun insert(client: Client) = viewModelScope.launch {
        repo.insert(client)
    }
}