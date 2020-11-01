package hu.bme.aut.android.timetic.ui.loginAregistration.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor

import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.network.models.ForMobileUserRegistration
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.ui.loginAregistration.Result

class LoginViewModel : ViewModel() {
    private var user: ForMobileUserRegistration? = null
    private var organisationUrl: String? = null

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> = _loginResult

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    fun login(email: String, password: String, organisationUrl: String?) {
        this.organisationUrl = organisationUrl
        val pref = MyApplication.secureSharedPreferences
        val editor = pref.edit()
        editor.putString("Email", email)
        editor.apply()
        if(organisationUrl != null){
            val backend =
                NetworkOrganisationInteractor(
                    organisationUrl,
                    HttpBasicAuth(email, password),
                    null
                )
            backend.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)
        }
        else{
            val backend =
                NetworkDeveloperInteractor(
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
        Log.d("EZAZ", "getToken succcccess RRRRRRRRRR")
        _refreshToken.value = token.token

        if(organisationUrl != null){
            getTokenOrganisation(token)
        }
        else{
            getTokenDeveloper(token)
        }
    }

    private fun errorRefreshToken(e: Throwable) {
        Log.d("EZAZ", "getToken errrrrror RRRRRRRRRR")
        _loginResult.value = Result(success = null, error = R.string.login_failed)
        //TODO
    }

    fun getTokenOrganisation(refreshToken: CommonToken){
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

    fun getTokenDeveloper(refreshToken: CommonToken){
        if(refreshToken.token != null){
            val n =
                NetworkDeveloperInteractor(
                    null,
                    HttpBearerAuth("bearer", refreshToken.token)
                )
            Log.d("EZAZ", "getToken")

            n.getToken(onSuccess = this::successToken, onError = this::errorToken)
        }
    }

    private fun successToken(token: CommonToken) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen succcccess")
        _token.value = token.token
        _loginResult.value = Result(success = true, error = null)

        //TODO
    }

    private fun errorToken(e: Throwable) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen errrrrror")
        //TODO
    }
}
