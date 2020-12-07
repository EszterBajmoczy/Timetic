package hu.bme.aut.android.timetic.views_viewmodels.chooseorganisations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.connectionmanager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization

class ChooseOrganizationViewModel : ViewModel() {
    private var backend: NetworkDeveloperInteractor = NetworkDeveloperInteractor(
            null,
            null
        )

    private val _organizationList = MutableLiveData<List<CommonOrganization>>()
    var organizationList: LiveData<List<CommonOrganization>> = _organizationList

    init {
        backend.getOrganizations(onSuccess = this::successList, onError = UseCases.Companion::logBackendError)
    }

    private fun successList(list: List<CommonOrganization>) {
        _organizationList.value = list
    }
}