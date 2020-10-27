package hu.bme.aut.android.timetic.ui.statistic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
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
        Log.d("EZAZ", "data success")
        _data.value = data
    }

    private fun error(e: Throwable) {
        Log.d("EZAZ", "data errrrrror")

        //TODO
    }
}
