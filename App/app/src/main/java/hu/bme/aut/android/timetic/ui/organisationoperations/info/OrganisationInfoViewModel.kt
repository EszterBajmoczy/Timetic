package hu.bme.aut.android.timetic.ui.organisationoperations.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.models.ForClientOrganization

class OrganisationInfoViewModel : ViewModel() {
    private val _info = MutableLiveData<ForClientOrganization>()
    var info: LiveData<ForClientOrganization> = _info

    fun getInformation(organisationUrl: String, email: String) {
        val backend: NetworkOrganisationInteractor = NetworkOrganisationInteractor(
            organisationUrl,
            null,
            null
        )

        backend.getOrganisationDataForClient(email, onSuccess = this::successList, onError = this::error)
    }

    private fun successList(list: ForClientOrganization) {
        _info.value = list
    }

    private fun error(e: Throwable, code: Int?, call: String) {
        when(code) {
            400 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "400 - Bad Request")
            401 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "401 - Unauthorized ")
            403 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "403 - Forbidden")
            404 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "404 - Not Found")
            409 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "409 - Conflict")
        }
        FirebaseCrashlytics.getInstance().setCustomKey("Call", call)
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}