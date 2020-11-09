package hu.bme.aut.android.timetic

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.ui.loginAregistration.login.afterTextChanged

class ForgottenPasswordActivity : AppCompatActivity() {
    private lateinit var viewModel: ForgottenPasswordViewModel
    private val pref = MyApplication.secureSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)


        viewModel = ViewModelProviders.of(this).get(ForgottenPasswordViewModel::class.java)

        val code = findViewById<EditText>(R.id.resetCode)
        val password = findViewById<EditText>(R.id.resetPassword)
        val passwordAgain = findViewById<EditText>(R.id.resetPasswordAgain)
        val reset = findViewById<Button>(R.id.bReset)
        val loading = findViewById<ProgressBar>(R.id.resetLoading)

        code.afterTextChanged {
            viewModel.codeDataChanged(
                code.text.toString(),
                password.text.toString(),
                passwordAgain.text.toString()
            )
        }

        password.afterTextChanged {
            viewModel.passwordsDataChanged(
                code.text.toString(),
                password.text.toString(),
                passwordAgain.text.toString()
            )
        }

        passwordAgain.afterTextChanged {
            viewModel.passwordsDataChanged(
                code.text.toString(),
                password.text.toString(),
                passwordAgain.text.toString()
            )
        }


        viewModel.loginResult.observe(this, Observer {
            Log.d("EZAZ", "Resetresult" + it.error + " : "+ it.success)
            val result = it ?: return@Observer

            loading.visibility = View.GONE
            if (result.error != null) {
                showResetFailed(result.error)
            }
            if (result.success != null) {
                //TODO login, save email stb
                updateUiWithUser()
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        viewModel.resetResult.observe(this, Observer {
            Log.d("EZAZ", "Resetresult" + it.error + " : "+ it.success)
            val result = it ?: return@Observer

            loading.visibility = View.GONE
            if (result.error != null) {
                showResetFailed(result.error)
            }
            if (result.success != null) {
                viewModel.login(email = MyApplication.getEmail()!!, password = password.text.toString())
            }
        })

        viewModel.resetForm.observe(this, Observer {
            val resetFormState = it ?: return@Observer

            // disable login button unless all value is valid
            reset.isEnabled = resetFormState.isDataValid

            if (resetFormState.code != null) {
                code.error = getString(resetFormState.code)
            }
            if (resetFormState.passwordError != null) {
                password.error = getString(resetFormState.passwordError)
            }
            if (resetFormState.passwordsNotMatchError != null) {
                passwordAgain.error = getString(resetFormState.passwordsNotMatchError)
            }
        })

        viewModel.refreshToken.observe(this, Observer {
            val editor = pref.edit()
            editor.putString("RefreshToken", it)
            editor.apply()
        })

        viewModel.token.observe(this, Observer {
            val editor = pref.edit()
            editor.putString("Token", it)
            editor.apply()
        })

        reset.setOnClickListener {
            viewModel.reset(MyApplication.Companion.ROLE.EMPLOYEE, MyApplication.getEmail()!!, code.text.toString().toInt(), password.text.toString())
        }
    }



    private fun showResetFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun updateUiWithUser() {
        val welcome = getString(R.string.welcome)
        Toast.makeText(
            applicationContext,
            welcome,
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}