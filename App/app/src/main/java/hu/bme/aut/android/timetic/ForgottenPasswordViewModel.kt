package hu.bme.aut.android.timetic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.network.models.CommonPasswordReset
import hu.bme.aut.android.timetic.network.models.CommonPostRefresh
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.ui.loginAregistration.Result

class ForgottenPasswordViewModel : ViewModel() {
    private lateinit var backendOrg: NetworkOrganisationInteractor
    private lateinit var backendDev: NetworkDeveloperInteractor
    private lateinit var email: String
    private var organisationUrl: String? = null
    private lateinit var role: Role
    private var tmpMap = HashMap<String, String>()
    private var mapSize = 0

    private val _resetForm = MutableLiveData<ResetFormState>()
    val resetForm: LiveData<ResetFormState> = _resetForm

    private val _resetResult = MutableLiveData<Result>()
    val resetResult: LiveData<Result> = _resetResult

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> = _loginResult

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _organisations = MutableLiveData<HashMap<String, String>>()
    val organisations: LiveData<HashMap<String, String>> = _organisations

    fun reset(role: Role, email: String, code: Int, password: String, organisationUrl: String?){
        when (role) {
            Role.EMPLOYEE -> {
                val backend =  NetworkOrganisationInteractor(
                    organisationUrl!!,
                    null,
                    null
                )
                val data = CommonPasswordReset(email, password, code)
                backend.saveNewPassword(data, onSuccess = this::onSuccesReset, onError = this::onErrorReset)
            }
            Role.CLIENT -> {
                val backend = NetworkDeveloperInteractor(
                    null,
                    null
                )
                val data = CommonPasswordReset(email, password, code)
                backend.saveNewPassword(data, onSuccess = this::onSuccesReset, onError = this::onErrorReset)
            }
        }

    }

    fun login(email: String, password: String, organisationUrl: String?) {
        this.organisationUrl = organisationUrl
        this.email = email

        if(organisationUrl.isNullOrEmpty()){
            role = Role.CLIENT
            val backend =
                NetworkDeveloperInteractor(
                    HttpBasicAuth(email, password),
                    null
                )
            backend.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)
        }
        else{

            role = Role.EMPLOYEE
            val backend =
                NetworkOrganisationInteractor(
                    organisationUrl,
                    HttpBasicAuth(email, password),
                    null
                )
            backend.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)
        }
    }

    private fun getTokenOrganisation(refreshToken: CommonToken){
        if(refreshToken.token != null){
            NetworkOrganisationInteractor(
                MyApplication.getOrganisationUrl()!!,
                null,
                HttpBearerAuth("bearer", refreshToken.token)
            ).getToken(onSuccess = this::successToken, onError = this::errorToken)
        }
    }

    private fun getTokenDeveloper(refreshToken: CommonToken){
        if(refreshToken.token != null){
            val n =
                NetworkDeveloperInteractor(
                    null,
                    HttpBearerAuth("bearer", refreshToken.token)
                )
            n.getToken(onSuccess = this::successToken, onError = this::errorToken)
        }
    }

    private fun successToken(token: CommonToken) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen succcccess reset")
        _token.value = token.token
        if(role == Role.CLIENT){
            val n =
                NetworkDeveloperInteractor(
                    null,
                    HttpBearerAuth("bearer", token.token!!)
                )
            n.getRegisteredOrganisations(onSuccess = this::successOrganisations, onError = this::error)
        } else {
            _loginResult.value = Result(success = true, error = null)
        }
    }

    private fun successOrganisations(organisations: List<CommonOrganization>) {
        _refreshToken.value?.let {token: String ->
            if(organisations.isEmpty()){
                _organisations.value = tmpMap
            }
            mapSize = organisations.size
            //get token for every organisation
            for(item in organisations) {
                val network =
                    NetworkOrganisationInteractor(
                        item.serverUrl!!,
                        null,
                        null
                    )
                network.postRefreshTokenForClient(CommonPostRefresh(email, token), onSuccess = this::successRefreshTokenForClient, onError = this::errorRefreshTokenForClient)
            }
        }
    }

    private fun successRefreshTokenForClient(token: CommonToken, url: String) {
        tmpMap[url] = token.token!!
        if(tmpMap.size == mapSize) {
            _organisations.value = tmpMap
        }
    }

    private fun errorRefreshTokenForClient(e: Throwable, code: Int?, call: String) {
        _loginResult.value = Result(success = true, error = R.string.login_failed)
        error(e, code, call)
    }

    private fun errorToken(e: Throwable, code: Int?, call: String) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen errrrrror reset")
        _loginResult.value = Result(success = null, error = R.string.login_failed)
        //TODO
    }

    private fun successRefreshToken(token: CommonToken) {
        Log.d("EZAZ", "getToken succcccess reset")
        _refreshToken.value = token.token
        when(role) {
            Role.EMPLOYEE -> getTokenOrganisation(token)
            Role.CLIENT -> getTokenDeveloper(token)
        }
    }

    private fun errorRefreshToken(e: Throwable, code: Int?, call: String) {
        Log.d("EZAZ", "getToken errrrrror reset")
        //TODO
    }

    private fun onSuccesReset(unit: Unit) {
        _resetResult.value = Result(success = true, error = null)
        Log.d("EZAZ", "succcccess reset password")
    }

    private fun onErrorReset(e: Throwable, code: Int?, call: String) {
        _resetResult.value = Result(success = null, error = R.string.user_not_found)
        Log.d("EZAZ", "errrrrror reset password")
        //TODO
    }

    fun codeDataChanged(code: String, password: String, passwordAgain: String) {
        if(code != ""){
            try {
                code.toInt()
                if (!isCodeValid(code)) {
                    _resetForm.value = ResetFormState(code = R.string.invalid_code_reset_empty)
                }
                else if (!isPasswordValid(password)) {
                    _resetForm.value = ResetFormState(passwordError = R.string.invalid_password_reset)
                } else if (password != passwordAgain) {
                    _resetForm.value = ResetFormState(passwordsNotMatchError = R.string.invalid_password2_reset)
                } else {
                    _resetForm.value = ResetFormState(isDataValid = true)
                }
            }catch (e: NumberFormatException) {
                _resetForm.value = ResetFormState(code = R.string.invalid_code_reset)
                return
            }
        }
    }

    fun passwordsDataChanged(code: String, password: String, passwordAgain: String) {
        if (!isCodeValid(code)) {
            _resetForm.value = ResetFormState(code = R.string.invalid_code_reset_empty)
        } else if (password != passwordAgain) {
            _resetForm.value = ResetFormState(passwordsNotMatchError = R.string.invalid_password2_reset)
        } else {
            _resetForm.value = ResetFormState(isDataValid = true)
        }
    }

    // Password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    // Code validation check
    private fun isCodeValid(password: String): Boolean {
        return password.length == 6
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