package hu.bme.aut.android.timetic.ui.loginAregistration.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor

import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.Role
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.network.models.CommonPostRefresh
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.ui.loginAregistration.Result

class LoginViewModel : ViewModel() {
    private lateinit var email: String
    private var organisationUrl: String? = null
    private lateinit var role: Role
    private var tmpMap = HashMap<String, String>()
    private var mapSize = 0

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> = _loginResult

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _resetResult = MutableLiveData<Result>()
    val resetResult: LiveData<Result> = _resetResult

    private val _organisations = MutableLiveData<HashMap<String, String>>()
    val organisations: LiveData<HashMap<String, String>> = _organisations

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

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // Email validation check
    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            false
        }
    }

    // Password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun successRefreshToken(token: CommonToken) {
        _refreshToken.value = token.token

        when(role) {
            Role.EMPLOYEE -> getTokenOrganisation(token)
            Role.CLIENT -> getTokenDeveloper(token)
        }
        Log.d("EZAZ", _refreshToken.value)
    }

    private fun errorRefreshToken(e: Throwable, code: Int?, call: String) {
        _loginResult.value = Result(success = null, error = R.string.login_failed)
        error(e, code, call)
    }

    private fun getTokenOrganisation(refreshToken: CommonToken){
        if(refreshToken.token != null){
            val n = organisationUrl?.let {
                NetworkOrganisationInteractor(
                    it,
                    null,
                    HttpBearerAuth("bearer", refreshToken.token)
                )
            }?.getToken(onSuccess = this::successToken, onError = this::errorToken)
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

    fun resetPassword(role: Role, email: String, organisationURL: String?){
        if(role == Role.EMPLOYEE){
            val backend =
                NetworkOrganisationInteractor(
                    organisationURL!!,
                    null,
                    null
                )
            backend.sendPasswordReset(email, onSuccess = this::onSuccesReset, onError = this::onErrorReset)
        }
        else {
            val backend =
                NetworkDeveloperInteractor(
                    null,
                    null
                )
            backend.sendPasswordReset(email, onSuccess = this::onSuccesReset, onError = this::onErrorReset)
        }
    }

    private fun successToken(token: CommonToken) {
        Log.d("EZAZ", token.token)
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
        _loginResult.value = Result(success = true, error = R.string.login_failed)
        error(e, code, call)
    }

    private fun onSuccesReset(unit: Unit) {
        _resetResult.value = Result(success = true, error = null)
    }

    private fun onErrorReset(e: Throwable, code: Int?, call: String) {
        _resetResult.value = Result(success = null, error = R.string.user_not_found)
        error(e, code, call)
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
