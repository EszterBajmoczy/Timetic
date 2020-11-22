package hu.bme.aut.android.timetic.ui.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.ForEmployeeReport

class StatisticDiagramViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor

    private val _data = MutableLiveData<ForEmployeeReport>()
    var data: LiveData<ForEmployeeReport> = _data

    fun fetchData(
        start: Long,
        end: Long,
        OrganisationUrl: String,
        Token: String
    ) {
        backend =
            NetworkOrganisationInteractor(
                OrganisationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    Token
                )
            )

        backend.getReport(start, end, onSuccess = this::success, onError = this::error)
    }

    private fun success(data: ForEmployeeReport) {
        _data.value = data
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
