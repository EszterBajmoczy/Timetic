package hu.bme.aut.android.timetic.ui.loginAregistration.registration

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor

import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.network.models.ForMobileUserRegistration
import hu.bme.aut.android.timetic.ui.loginAregistration.Result

class RegistrationViewModel() : ViewModel() {
    private var user: ForMobileUserRegistration? = null

    private val _loginForm = MutableLiveData<RegistrationFormState>()
    val registrationFormState: LiveData<RegistrationFormState> = _loginForm

    private val _loginResult = MutableLiveData<Result>()
    val result: LiveData<Result> = _loginResult

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    fun login(email: String, password: String) {
        val n =
            NetworkDeveloperInteractor(
                HttpBasicAuth(email, password),
                null
            )
        Log.d("EZAZ", "getToken")

        n.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)
    }

    fun register(name: String, email: String, password: String) {
        user =
            ForMobileUserRegistration(
                name,
                email,
                password
            )
        val n =
            NetworkDeveloperInteractor(
                null,
                null
            )
        n.registerUser(
            ForMobileUserRegistration(
                name,
                email,
                password
            ), onSuccess = this::successReg, onError = this::errorReg)
    }

    fun loginDataChanged(name: String, email: String, password: String) {
        if (!isNameValid(name)) {
            _loginForm.value = RegistrationFormState(usernameError = R.string.invalid_name)
        } else if (!isEmailValid(email)) {
            _loginForm.value = RegistrationFormState(emailError = R.string.invalid_email)
        }else if (!isPasswordValid(password)) {
            _loginForm.value = RegistrationFormState(passwordError = R.string.invalid_password)
        }else {
            _loginForm.value = RegistrationFormState(isDataValid = true)
        }
    }

    // Name validation check
    private fun isNameValid(name: String): Boolean {
        return name.contains(' ')
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
        Log.d("EZAZ", "login succcccess")
        _loginResult.value =
            Result(
                success = true,
                error = null
            )
        _refreshToken.value = token.token
        getToken(token)
        //TODO
    }

    private fun errorRefreshToken(e: Throwable) {
        Log.d("EZAZ", "login errrrrror")
        _loginResult.value =
            Result(
                success = null,
                error = R.string.login_failed
            )
        //TODO
    }

    private fun getToken(refreshToken: CommonToken){
        if(refreshToken.token != null){
            val n =
                NetworkDeveloperInteractor(
                    null,
                    HttpBearerAuth("bearer", refreshToken.token)
                )
            Log.d("EZAZ", "gettoken")

            n.getToken(onSuccess = this::successToken, onError = this::errorToken)
        }
    }

    private fun successToken(token: CommonToken) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen succcccess")
        _token.value = token.token
        //TODO
    }

    private fun errorToken(e: Throwable) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen errrrrror")
        //TODO
    }

    private fun successReg(u: Unit) {
        Log.d("EZAZ", "Registration succcccess")
        try {
            user?.let {
                val n =
                    NetworkDeveloperInteractor(
                        HttpBasicAuth(user!!.email!!, user!!.password!!),
                        null
                    )
                Log.d("EZAZ", "login")

                n.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)

            }
        }catch (e: Throwable){
            errorRefreshToken(e)
        }
    }

    private fun errorReg(e: Throwable) {
        Log.d("EZAZ", "Registration failed")
        //TODO
    }
}
