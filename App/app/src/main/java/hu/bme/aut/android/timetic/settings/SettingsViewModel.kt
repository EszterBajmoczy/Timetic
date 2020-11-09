package hu.bme.aut.android.timetic.settings

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.models.ForEmployeeDataForAppointmentCreation
import kotlin.collections.ArrayList

class SettingsViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor
    private var repo: DBRepository

    private val _activities = MutableLiveData<List<String>>()
    val activities: LiveData<List<String>> = _activities

    init{
        val dao = MyApplication.myDatabase.roomDao()
        Log.d("EZAZ", "settings")
        repo = DBRepository(dao)
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
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = this::error)
    }

    private fun successDataForCreation(data: ForEmployeeDataForAppointmentCreation) {
        val stringList = ArrayList<String>()
        data.activities.let {
            if (it != null) {
                for(item in it){
                    stringList.add(item.name!!)
                }
            }
        }
        _activities.value = stringList
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