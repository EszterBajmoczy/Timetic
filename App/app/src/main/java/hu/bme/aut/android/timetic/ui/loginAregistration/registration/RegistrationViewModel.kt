package hu.bme.aut.android.timetic.ui.loginAregistration.registration

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor

import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.Role
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.network.models.CommonPostRefresh
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
        _refreshToken.value = token.token
        getToken(token)
    }

    private fun errorRefreshToken(e: Throwable, code: Int?, call: String) {
        Log.d("EZAZ", "login errrrrror")
        _loginResult.value =
            Result(
                success = null,
                error = R.string.login_failed
            )
        error(e, code, call)
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
        _token.value = token.token
        //no need to check the registered organisations, because it's a registration!
        _loginResult.value =
            Result(
                success = true,
                error = null
            )
    }

    private fun errorToken(e: Throwable, code: Int?, call: String) {
        _loginResult.value =
            Result(
                success = null,
                error = R.string.login_failed
            )
        error(e, code, call)
    }

    private fun successReg(u: Unit) {
        Log.d("EZAZ", "Registration succcccess")
        val n =
            NetworkDeveloperInteractor(
                HttpBasicAuth(user!!.email!!, user!!.password!!),
                null
            )
        Log.d("EZAZ", "login")

        n.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)
    }

    private fun errorReg(e: Throwable, code: Int?, call: String) {
        //TODO inform view
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
