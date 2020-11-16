package hu.bme.aut.android.timetic.ui.organisationoperations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization

class OrganisationOperationViewModel : ViewModel() {
    private val _organisations = MutableLiveData<List<CommonOrganization>>()
    var organisations: LiveData<List<CommonOrganization>> = _organisations

    fun fetchData() {
        val backend =
            NetworkDeveloperInteractor(
                null,
                null
            )
        backend.getOrganisations(onSuccess = this::success, onError = this::error)
    }

    private fun success(list: List<CommonOrganization>) {
        _organisations.value = list
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