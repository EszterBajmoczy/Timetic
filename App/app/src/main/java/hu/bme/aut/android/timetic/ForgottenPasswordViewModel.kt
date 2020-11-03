package hu.bme.aut.android.timetic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonPasswordReset
import hu.bme.aut.android.timetic.network.models.CommonToken
import hu.bme.aut.android.timetic.ui.loginAregistration.Result

class ForgottenPasswordViewModel : ViewModel() {
    private lateinit var backendOrganisation: NetworkOrganisationInteractor
    private lateinit var backendDeveloper: NetworkDeveloperInteractor

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

    fun reset(role: MyApplication.Companion.ROLE, email: String, code: Int, password: String){
        when (role) {
            MyApplication.Companion.ROLE.EMPLOYEE -> {
                backendOrganisation =  NetworkOrganisationInteractor(
                    MyApplication.getOrganisationUrl()!!,
                    null,
                    null
                )
                val data = CommonPasswordReset(email, password, code)
                backendOrganisation.saveNewPassword(data, onSuccess = this::onSuccesReset, onError = this::onErrorReset)
            }
            MyApplication.Companion.ROLE.CLIENT -> {
                backendDeveloper = NetworkDeveloperInteractor(
                    null,
                    null
                )
            }
        }

    }

    fun login(email: String, password: String) {
        //TODO role
        val backend =
            NetworkOrganisationInteractor(
                MyApplication.getOrganisationUrl()!!,
                HttpBasicAuth(email, password),
                null
            )
        backend.login(onSuccess = this::successRefreshToken, onError = this::errorRefreshToken)
    }

    fun getTokenOrganisation(refreshToken: CommonToken){
        if(refreshToken.token != null){
            NetworkOrganisationInteractor(
                MyApplication.getOrganisationUrl()!!,
                null,
                HttpBearerAuth("bearer", refreshToken.token)
            ).getToken(onSuccess = this::successToken, onError = this::errorToken)
        }
    }

    private fun successToken(token: CommonToken) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen succcccess reset")
        _token.value = token.token
        _loginResult.value = Result(success = true, error = null)

        //TODO
    }

    private fun errorToken(e: Throwable) {
        Log.d("EZAZ", "getTokeeeeeeeeeeeeeeeeeeeeeeeeeeen errrrrror reset")
        _loginResult.value = Result(success = null, error = R.string.login_failed)
        //TODO
    }

    private fun successRefreshToken(token: CommonToken) {
        Log.d("EZAZ", "getToken succcccess reset")
        _refreshToken.value = token.token
        //TODO role
        getTokenOrganisation(token)
    }

    private fun errorRefreshToken(e: Throwable) {
        Log.d("EZAZ", "getToken errrrrror reset")
        //TODO
    }

    private fun onSuccesReset(unit: Unit) {
        _resetResult.value = Result(success = true, error = null)
        Log.d("EZAZ", "succcccess reset password")
    }

    private fun onErrorReset(e: Throwable) {
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
}