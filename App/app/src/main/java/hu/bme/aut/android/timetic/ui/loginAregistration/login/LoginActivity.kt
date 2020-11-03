package hu.bme.aut.android.timetic.ui.loginAregistration.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.ForgottenPasswordActivity
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private var organisationURL: String? = null
    private var emailAccount: String? = null

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences = MyApplication.secureSharedPreferences

        organisationURL = intent.getStringExtra("OrganisationUrl")

        val email = findViewById<EditText>(R.id.logEmail)
        val password = findViewById<EditText>(R.id.logPassword)
        val passwordForgotten = findViewById<Button>(R.id.btPasswordForgotten)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.logLoading)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
                .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                email.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            Log.d("EZAZ", "Loginresult" + it.error + " : "+ it.success)
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser()
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        loginViewModel.refreshToken.observe(this@LoginActivity, Observer {
            val editor = sharedPreferences.edit()
            editor.putString("RefreshToken", it)
            editor.apply()
        })

        loginViewModel.token.observe(this@LoginActivity, Observer {
            val editor = sharedPreferences.edit()
            editor.putString("Token", it)
            editor.apply()
        })

        loginViewModel.resetResult.observe(this@LoginActivity, Observer {
            Log.d("EZAZ", "Resetresult" + it.error + " : "+ it.success)
            val resetResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (resetResult.error != null) {
                showLoginFailed(resetResult.error)
            }
            if (resetResult.success != null) {
                val intent = Intent(this, ForgottenPasswordActivity::class.java)
                startActivity(intent)
            }
        })

        //TODO
        passwordForgotten.setOnClickListener {
            if(email.text.toString() != ""){
                val editor = sharedPreferences.edit()
                editor.putString("Email", email.text.toString())
                editor.apply()

                loginViewModel.resetPassword(email.text.toString(), organisationURL!!)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Adja meg az e-mail címét",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {

                    EditorInfo.IME_ACTION_DONE ->{
                        loginViewModel.login(
                            email.text.toString(),
                            password.text.toString(),
                            organisationURL
                        )
                        emailAccount = email.text.toString()
                    }
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(email.text.toString(), password.text.toString(), organisationURL)
                emailAccount = email.text.toString()
            }
        }
    }

    private fun updateUiWithUser() {
        val welcome = getString(R.string.welcome)
        Toast.makeText(
                applicationContext,
                welcome,
                Toast.LENGTH_LONG
        ).show()

        val editor = sharedPreferences.edit()
        editor.putString("Email", emailAccount)
        editor.apply()

        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
