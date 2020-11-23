package hu.bme.aut.android.timetic.ui.organizationoperations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization

class OrganizationOperationViewModel : ViewModel() {
    private val _organizations = MutableLiveData<List<CommonOrganization>>()
    var organizations: LiveData<List<CommonOrganization>> = _organizations

    fun fetchData() {
        val backend =
            NetworkDeveloperInteractor(
                null,
                null
            )
        backend.getOrganizations(onSuccess = this::success, onError = UseCases.Companion::logBackendError)
    }

    private fun success(list: List<CommonOrganization>) {
        _organizations.value = list
    }
}