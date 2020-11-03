package hu.bme.aut.android.timetic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization

class ChooseOrganisationViewModel : ViewModel() {
    private var backend: NetworkDeveloperInteractor = NetworkDeveloperInteractor(
            null,
            null
        )

    private val _organisationList = MutableLiveData<List<CommonOrganization>>()
    var organisationList: LiveData<List<CommonOrganization>> = _organisationList

    init {
        backend.getOrganisations(onSuccess = this::successList, onError = this::error)
    }

    private fun successList(list: List<CommonOrganization>) {
        Log.d("EZAZ", "succcccess")
        _organisationList.value = list
    }

    private fun error(e: Throwable) {
        Log.d("EZAZ", "errrrrror")
        //TODO
    }
}