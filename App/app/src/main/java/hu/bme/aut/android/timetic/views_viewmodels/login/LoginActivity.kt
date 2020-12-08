package hu.bme.aut.android.timetic.views_viewmodels.login

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
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
import hu.bme.aut.android.timetic.*
import hu.bme.aut.android.timetic.views_viewmodels.forgottenpassword.ForgottenPasswordActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private var emailAccount: String? = null
    private lateinit var role: Role

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences = MyApplication.secureSharedPreferences

        val organizationURL = intent.getStringExtra("OrganizationUrl")

        role = if(organizationURL == null){
            Role.CLIENT
        } else {
            Role.EMPLOYEE
        }

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

        loginViewModel.userName.observe(this, Observer {
            val editor = sharedPreferences.edit()
            editor.putString("UserName", it)
            editor.apply()
        })

        loginViewModel.refreshToken.observe(this@LoginActivity, Observer {
            val editor = sharedPreferences.edit()
            editor.putString("RefreshToken", it)
            editor.apply()
        })

        loginViewModel.token.observe(this@LoginActivity, Observer {
            if(role == Role.CLIENT){
                val editor = sharedPreferences.edit()
                editor.putString("DevToken", it)
                editor.apply()
            } else {
                val editor = sharedPreferences.edit()
                editor.putString("Token", it)
                editor.apply()
            }
        })

        loginViewModel.resetResult.observe(this@LoginActivity, Observer {
            val resetResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (resetResult.error != null) {
                showLoginFailed(resetResult.error)
            }
            if (resetResult.success != null) {
                val intent = Intent(this, ForgottenPasswordActivity::class.java)
                if(organizationURL != null) {
                    intent.putExtra("OrganizationUrl", organizationURL)
                } else {
                    intent.putExtra("OrganizationUrl", "")
                }
                startActivity(intent)
            }
        })

        loginViewModel.organizations.observe(this, Observer { map ->
            val editor = MyApplication.secureSharedPreferences.edit()
            editor.putString("OrganizationsMap", map.toString())
            editor.apply()

            updateUiWithUser()
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        passwordForgotten.setOnClickListener {
            if(isNetworkConnection()) {
                if(email.text.toString() != ""){
                    val editor = sharedPreferences.edit()
                    editor.putString("Email", email.text.toString())
                    editor.apply()

                    loginViewModel.resetPassword(role, email.text.toString(), organizationURL)
                } else {
                    Toast.makeText(applicationContext, getString(R.string.add_email_adress), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(applicationContext, getString(R.string.network_needed_password_reset), Toast.LENGTH_LONG).show()
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
                            organizationURL
                        )
                        emailAccount = email.text.toString()
                    }
                }
                false
            }

            login.setOnClickListener {
                if(isNetworkConnection()){
                    loading.visibility = View.VISIBLE
                    loginViewModel.login(email.text.toString(), password.text.toString(), organizationURL)
                    emailAccount = email.text.toString()
                } else {
                    Toast.makeText(context, getString(R.string.network_needed_log_in), Toast.LENGTH_LONG).show()
                }
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun isNetworkConnection(): Boolean {
        val connectivityManager = applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
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
