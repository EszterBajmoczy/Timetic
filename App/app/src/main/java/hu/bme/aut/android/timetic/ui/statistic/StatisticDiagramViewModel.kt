package hu.bme.aut.android.timetic.ui.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.Singleton
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.ForEmployeeReport

class StatisticDiagramViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganizationInteractor

    private val _data = MutableLiveData<ForEmployeeReport>()
    var data: LiveData<ForEmployeeReport> = _data

    fun fetchData(
        start: Long,
        end: Long,
        OrganizationUrl: String,
        Token: String
    ) {
        backend =
            NetworkOrganizationInteractor(
                OrganizationUrl,
                null,
                HttpBearerAuth(
                    "bearer",
                    Token
                )
            )

        backend.getReport(start, end, onSuccess = this::success, onError = Singleton::logBackendError)
    }

    private fun success(data: ForEmployeeReport) {
        _data.value = data
    }
}
