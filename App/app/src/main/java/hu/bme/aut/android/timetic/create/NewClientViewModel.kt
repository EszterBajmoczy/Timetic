package hu.bme.aut.android.timetic.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.*
import kotlinx.coroutines.launch


class NewClientViewModel : ViewModel() {
    private lateinit var backend: NetworkOrganisationInteractor
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

    fun initialize(organisationUrl: String, token: String? = null){
        if(token == null){
            backend =
                NetworkOrganisationInteractor(
                    organisationUrl,
                    null,
                    null
                )
        } else {
            backend =
                NetworkOrganisationInteractor(
                    organisationUrl,
                    null,
                    HttpBearerAuth(
                        "bearer",
                        token
                    )
                )
            backend.getOrganisationDataForEmployee(onSuccess = this::onSuccess, onError = this::error)
        }
    }

    fun sendOrganisationIdToDev(devToken: String, organisationId: String) {
        val devBackend =
            NetworkDeveloperInteractor(
                null,
                    HttpBearerAuth(
                        "bearer",
                        devToken
                    )
                )
        devBackend.patchRegisteredOrganisationById(organisationId = organisationId,
            onSuccess = this::onSuccessAddOrganisationIdToDev, onError = this::onErrorAddOrganisationIdToDev)
    }

    fun onSuccessAddOrganisationIdToDev(unit: Unit){
        _clientRegistered.value = true
    }

    fun onErrorAddOrganisationIdToDev(e: Throwable, code: Int?, call: String){
        _clientRegistered.value = false
        error(e, code, call)
    }

    fun onSuccess(data: ForEmployeeOrganization){
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

    fun getTokenForClient(organisationUrl: String, email: String, refreshToken: String) {
        val backendWithoutAuth =
            NetworkOrganisationInteractor(
                organisationUrl,
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
        Log.d("EZAZ", "add clienttttttttttt")
        _success.value = true
        val c = Person(backendId = client.id!!, name = client.name!!, email = client.email!!, phone = client.phone!!)

        insert(c)
    }

    private fun onErrorClientAdd(e: Throwable, code: Int?, call: String){
        _success.value = false
        error(e, code, call)
    }

    private fun insert(person: Person) = viewModelScope.launch {
        repo.insert(person)
    }
}