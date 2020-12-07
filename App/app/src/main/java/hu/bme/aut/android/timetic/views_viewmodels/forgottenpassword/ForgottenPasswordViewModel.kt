package hu.bme.aut.android.timetic.views_viewmodels.forgottenpassword

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.*
import hu.bme.aut.android.timetic.connectionmanager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.connectionmanager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.*
import hu.bme.aut.android.timetic.Result
import hu.bme.aut.android.timetic.forms.ResetFormState

class ForgottenPasswordViewModel : ViewModel() {
    private lateinit var email: String
    private var organizationUrl: String? = null
    private lateinit var role: Role
    private var tmpMap = HashMap<String, String>()
    private var mapSize = 0

    private val _resetForm = MutableLiveData<ResetFormState>()
    val resetForm: LiveData<ResetFormState> = _resetForm

    private val _resetResult = MutableLiveData<Result>()
    val resetResult: LiveData<Result> = _resetResult

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> = _loginResult

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _organizations = MutableLiveData<HashMap<String, String>>()
    val organizations: LiveData<HashMap<String, String>> = _organizations

    fun reset(role: Role, email: String, code: Int, password: String, organizationUrl: String?){
        when (role) {
            Role.EMPLOYEE -> {
                val backend =  NetworkOrganizationInteractor(
                    organizationUrl!!,
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
            backend.login(onSuccess = this::successLoginClient, onError = UseCases.Companion::logBackendError)
        }
        else{

            role = Role.EMPLOYEE
            val backend =
                NetworkOrganizationInteractor(
                    organizationUrl,
                    HttpBasicAuth(email, password),
                    null
                )
            backend.login(onSuccess = this::successLoginEmployee, onError = UseCases.Companion::logBackendError)
        }
    }

    private fun getTokenOrganization(refreshToken: CommonToken){
        if(refreshToken.token != null){
            NetworkOrganizationInteractor(
                MyApplication.getOrganizationUrl()!!,
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
        UseCases.logBackendError(
            e,
            code,
            call
        )
    }

    private fun errorToken(e: Throwable, code: Int?, call: String) {
        _loginResult.value = Result(
            success = null,
            error = R.string.login_failed
        )
        UseCases.logBackendError(
            e,
            code,
            call
        )
    }

    private fun successLoginClient(loginData: ForUserLoginData) {
        _userName.value = loginData.user!!.name
        setRefreshToken(loginData.refreshToken!!)
    }

    private fun successLoginEmployee(loginData: ForEmployeeLoginData) {
        _userName.value = loginData.employee!!.name
        setRefreshToken(loginData.refreshToken!!)
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
        UseCases.logBackendError(
            e,
            code,
            call
        )
    }

    fun codeDataChanged(code: String, password: String, passwordAgain: String) {
        if(code != ""){
            try {
                code.toInt()
                if (!isCodeValid(code)) {
                    _resetForm.value =
                        ResetFormState(code = R.string.invalid_code_reset_empty)
                }
                else if (!isPasswordValid(password)) {
                    _resetForm.value =
                        ResetFormState(
                            passwordError = R.string.invalid_password
                        )
                } else if (password != passwordAgain) {
                    _resetForm.value =
                        ResetFormState(
                            passwordsNotMatchError = R.string.invalid_password2_reset
                        )
                } else {
                    _resetForm.value =
                        ResetFormState(
                            isDataValid = true
                        )
                }
            }catch (e: NumberFormatException) {
                _resetForm.value =
                    ResetFormState(code = R.string.invalid_code_reset)
                return
            }
        }
    }

    fun passwordsDataChanged(code: String, password: String, passwordAgain: String) {
        if (!isCodeValid(code)) {
            _resetForm.value =
                ResetFormState(code = R.string.invalid_code_reset_empty)
        } else if (password != passwordAgain) {
            _resetForm.value =
                ResetFormState(
                    passwordsNotMatchError = R.string.invalid_password2_reset
                )
        } else {
            _resetForm.value =
                ResetFormState(isDataValid = true)
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

    private fun setRefreshToken(token: CommonToken){
        _refreshToken.value = token.token

        when(role) {
            Role.EMPLOYEE -> getTokenOrganization(token)
            Role.CLIENT -> getTokenDeveloper(token)
        }
    }
}