package hu.bme.aut.android.timetic.ui.organizationoperations.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.models.ForClientOrganization

class OrganizationInfoViewModel : ViewModel() {
    private val _info = MutableLiveData<ForClientOrganization>()
    var info: LiveData<ForClientOrganization> = _info

    fun getInformation(organizationUrl: String, email: String) {
        val backend: NetworkOrganizationInteractor = NetworkOrganizationInteractor(
            organizationUrl,
            null,
            null
        )

        backend.getOrganizationDataForClient(email, onSuccess = this::successList, onError = UseCases.Companion::logBackendError)
    }

    private fun successList(list: ForClientOrganization) {
        _info.value = list
    }
}