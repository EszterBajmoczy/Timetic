package hu.bme.aut.android.timetic.views_viewmodels.settings

import androidx.lifecycle.*
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.connectionmanager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.ForEmployeeDataForAppointmentCreation
import hu.bme.aut.android.timetic.repository.DBRepository
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class SettingsViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganizationInteractor

    private val _activities = MutableLiveData<List<String>>()
    val activities: LiveData<List<String>> = _activities

    private val _locations = MutableLiveData<List<String>>()
    val locations: LiveData<List<String>> = _locations

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
        backend.getDataForAppointmentCreation(onSuccess = this::successDataForCreation, onError = UseCases.Companion::logBackendError)
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
        _locations.value = data.places
    }

    fun deleteAllFromProject() = viewModelScope.launch {
        val dao = MyApplication.myDatabase.roomDao()
        val repo = DBRepository(dao)
        repo.deleteAllTables()
    }
}