package hu.bme.aut.android.timetic.views_viewmodels.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import hu.bme.aut.android.timetic.connectionmanager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.connectionmanager.NetworkOrganizationInteractor

import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.Role
import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.*
import hu.bme.aut.android.timetic.Result
import hu.bme.aut.android.timetic.forms.LoginFormState

class LoginViewModel : ViewModel() {
    private lateinit var email: String
    private var organizationUrl: String? = null
    private lateinit var role: Role
    private var tmpMap = HashMap<String, String>()
    private var mapSize = 0

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> = _loginResult

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _resetResult = MutableLiveData<Result>()
    val resetResult: LiveData<Result> = _resetResult

    private val _organizations = MutableLiveData<HashMap<String, String>>()
    val organizations: LiveData<HashMap<String, String>> = _organizations

    fun login(email: String, password: String, organizationUrl: String?) {
        this.organizationUrl = organizationUrl
        this.email = email

        if(organizationUrl.isNullOrEmpty()){
            role = Role.CLIENT
            val backend =
                NetworkDeveloperInteractor(
                    HttpBasicAuth(email, password),
                    null
                )
            backend.login(onSuccess = this::successRefreshTokenClient, onError = this::errorRefreshToken)
        }
        else{
            role = Role.EMPLOYEE
            val backend =
                NetworkOrganizationInteractor(
                    organizationUrl,
                    HttpBasicAuth(email, password),
                    null
                )
            backend.login(onSuccess = this::successRefreshTokenEmployee, onError = this::errorRefreshToken)
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value =
                LoginFormState(usernameError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value =
                LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value =
                LoginFormState(isDataValid = true)
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

    private fun successRefreshTokenClient(loginData: ForUserLoginData) {
        _userName.value = loginData.user!!.name
        setRefreshToken(loginData.refreshToken!!)
    }

    private fun successRefreshTokenEmployee(loginData: ForEmployeeLoginData) {
        _userName.value = loginData.employee!!.name
        setRefreshToken(loginData.refreshToken!!)
    }

    private fun errorRefreshToken(e: Throwable, code: Int?, call: String) {
        _loginResult.value = Result(
            success = null,
            error = R.string.login_failed
        )
        UseCases.logBackendError(e, code, call)
    }

    private fun getTokenOrganization(refreshToken: CommonToken){
        if(refreshToken.token != null){
            organizationUrl?.let {
                NetworkOrganizationInteractor(
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

    fun resetPassword(role: Role, email: String, organizationURL: String?){
        if(role == Role.EMPLOYEE){
            val backend =
                NetworkOrganizationInteractor(
                    organizationURL!!,
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
            n.getRegisteredOrganizations(onSuccess = this::successOrganizations, onError = UseCases.Companion::logBackendError)
        } else {
            _loginResult.value =
                Result(success = true, error = null)
        }
    }

    private fun successOrganizations(organizations: List<CommonOrganization>) {
        _refreshToken.value?.let {token: String ->
            if(organizations.isEmpty()){
                _organizations.value = tmpMap
            }
            mapSize = organizations.size
            //get token for every organization
            for(item in organizations) {
                val network =
                    NetworkOrganizationInteractor(
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
            _organizations.value = tmpMap
        }
    }

    private fun errorRefreshTokenForClient(e: Throwable, code: Int?, call: String) {
        _loginResult.value = Result(
            success = true,
            error = R.string.login_failed
        )
        UseCases.logBackendError(e, code, call)
    }

    private fun errorToken(e: Throwable, code: Int?, call: String) {
        _loginResult.value = Result(
            success = true,
            error = R.string.login_failed
        )
        UseCases.logBackendError(e, code, call)
    }

    private fun onSuccesReset(unit: Unit) {
        _resetResult.value =
            Result(success = true, error = null)
    }

    private fun onErrorReset(e: Throwable, code: Int?, call: String) {
        _resetResult.value = Result(
            success = null,
            error = R.string.user_not_found
        )
        UseCases.logBackendError(e, code, call)
    }

    private fun setRefreshToken(token: CommonToken){
        _refreshToken.value = token.token

        when(role) {
            Role.EMPLOYEE -> getTokenOrganization(token)
            Role.CLIENT -> getTokenDeveloper(token)
        }
    }
}
