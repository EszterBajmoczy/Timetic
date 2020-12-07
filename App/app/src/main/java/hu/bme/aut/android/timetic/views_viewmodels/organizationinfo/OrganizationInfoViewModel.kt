package hu.bme.aut.android.timetic.views_viewmodels.organizationinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.connectionmanager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.connectionmanager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonPostRefresh
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.network.models.ForClientOrganization
import hu.bme.aut.android.timetic.repository.DBRepository
import kotlinx.coroutines.launch

class OrganizationInfoViewModel : ViewModel() {
    private val _info = MutableLiveData<ForClientOrganization>()
    var info: LiveData<ForClientOrganization> = _info

    private val _clientRegistered = MutableLiveData<Boolean>()
    val clientRegistered: LiveData<Boolean> = _clientRegistered

    private val _tokenSuccess = MutableLiveData<List<String>>()
    val tokenSuccess: LiveData<List<String>> = _tokenSuccess

    fun getInformation(organizationUrl: String, email: String) {
        val backend = NetworkOrganizationInteractor(
            organizationUrl,
            null,
            null
        )

        backend.getOrganizationDataForClient(email, onSuccess = this::successList, onError = UseCases.Companion::logBackendError)
    }

    private fun successList(list: ForClientOrganization) {
        _info.value = list
    }

    fun sendOrganizationIdToDev(devToken: String, organizationId: String) {
        val devBackend =
            NetworkDeveloperInteractor(
                null,
                HttpBearerAuth(
                    "bearer",
                    devToken
                )
            )
        devBackend.patchRegisteredOrganizationById(organizationId = organizationId,
            onSuccess = this::onSuccessAddOrganizationIdToDev, onError = this::onErrorAddOrganizationIdToDev)
    }

    fun onSuccessAddOrganizationIdToDev(unit: Unit){
        _clientRegistered.value = true
    }

    fun onErrorAddOrganizationIdToDev(e: Throwable, code: Int?, call: String){
        _clientRegistered.value = false
        UseCases.logBackendError(e, code, call)
    }

    fun getTokenForClient(organizationUrl: String, email: String, refreshToken: String) {
        val backendWithoutAuth =
            NetworkOrganizationInteractor(
                organizationUrl,
                null,
                null
            )
        backendWithoutAuth.postRefreshTokenForClient(CommonPostRefresh(email, refreshToken), onSuccess = this::onSuccessPutRefreshToken, onError = this::onErrorClientAdd)
    }
    private fun onSuccessPutRefreshToken(token: CommonToken, url:String){
        val list = ArrayList<String>()
        list.add(url)
        list.add(token.token!!)
        _tokenSuccess.value = list
    }
    fun onErrorClientAdd(e: Throwable, code: Int?, call: String){
        _clientRegistered.value = false
        UseCases.logBackendError(e, code, call)
    }

    fun deleteAllFromProject() = viewModelScope.launch {
        val dao = MyApplication.myDatabase.roomDao()
        val repo = DBRepository(dao)
        repo.deleteAllTables()
    }
}