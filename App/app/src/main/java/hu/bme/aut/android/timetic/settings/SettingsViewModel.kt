package hu.bme.aut.android.timetic.settings

import androidx.lifecycle.*
import hu.bme.aut.android.timetic.Singleton
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.ForEmployeeDataForAppointmentCreation
import kotlin.collections.ArrayList

class SettingsViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganizationInteractor

    private val _activities = MutableLiveData<List<String>>()
    val activities: LiveData<List<String>> = _activities

    fun getDataForAppointmentCreation(organizationUrl: String, token: String){
        backend =
            NetworkOrganizationInteractor(
                organizationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    token
                )
            )
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = Singleton::logBackendError)
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
}