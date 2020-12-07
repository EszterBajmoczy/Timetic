package hu.bme.aut.android.timetic.views_viewmodels.newclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.models.Person
import hu.bme.aut.android.timetic.repository.DBRepository
import hu.bme.aut.android.timetic.connectionmanager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.connectionmanager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.*
import kotlinx.coroutines.launch


class NewClientViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganizationInteractor
    private var repo: DBRepository

    private val _data = MutableLiveData<ForEmployeeOrganization>()
    val data: LiveData<ForEmployeeOrganization> = _data

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    private val _tokenForClient = MutableLiveData<String>()
    val tokenForClient: LiveData<String> = _tokenForClient

    private val _clientRegistered = MutableLiveData<Boolean>()
    val clientRegistered: LiveData<Boolean> = _clientRegistered

    init {
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)
    }

    fun initialize(organizationUrl: String, token: String? = null){
        if(token == null){
            backend =
                NetworkOrganizationInteractor(
                    organizationUrl,
                    null,
                    null
                )
        } else {
            backend =
                NetworkOrganizationInteractor(
                    organizationUrl,
                    null,
                    HttpBearerAuth(
                        "bearer",
                        token
                    )
                )
            backend.getOrganizationDataForEmployee(onSuccess = this::onSuccess, onError = UseCases.Companion::logBackendError)
        }
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

    fun onSuccess(data: ForEmployeeOrganization){
        _data.value = data
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
        _tokenForClient.value = token.token
    }

    fun registerClient(c: CommonClient) {
        backend.registerClient(client = c, onSuccess = this::onSuccessClientAddByClient, onError = this::onErrorClientAdd)
    }

    private fun onSuccessClientAddByClient(unit: Unit){
        _success.value = true
    }

    fun addClient(c: CommonClient) {
        backend.addClient(client = c, onSuccess = this::onSuccessClientAdd, onError = this::onErrorClientAdd)
    }

    private fun onSuccessClientAdd(client: CommonClient){
        _success.value = true
        val c = Person(backendId = client.id!!, name = client.name!!, email = client.email!!, phone = client.phone!!)

        insert(c)
    }

    private fun onErrorClientAdd(e: Throwable, code: Int?, call: String){
        _success.value = false
        UseCases.logBackendError(e, code, call)
    }

    private fun insert(person: Person) = viewModelScope.launch {
        repo.insert(person)
    }
}