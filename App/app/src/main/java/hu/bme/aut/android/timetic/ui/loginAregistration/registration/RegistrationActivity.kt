package hu.bme.aut.android.timetic.ui.loginAregistration.registration

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.MyApplication

import hu.bme.aut.android.timetic.R
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var registrationViewModel: RegistrationViewModel
    private var organisationURL: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        organisationURL = intent.getStringExtra("OrganisationUrl")

        val name = findViewById<EditText>(R.id.regUsername)
        val email = findViewById<EditText>(R.id.regEmail)
        val password = findViewById<EditText>(R.id.regPassword)
        val register = findViewById<Button>(R.id.register)
        val loading = findViewById<ProgressBar>(R.id.regLoading)

        registrationViewModel = ViewModelProviders.of(this, RegistrationViewModelFactory())
                .get(RegistrationViewModel::class.java)

        registrationViewModel.registrationFormState.observe(this@RegistrationActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            register.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                name.error = getString(loginState.usernameError)
            }
            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        registrationViewModel.result.observe(this@RegistrationActivity, Observer {
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
        sharedPreferences = MyApplication.secureSharedPreferences

        registrationViewModel.refreshToken.observe(this@RegistrationActivity, Observer {
            val editor = sharedPreferences.edit()
            editor.putString("RefreshToken", it)
            editor.apply()

        })

        registrationViewModel.token.observe(this@RegistrationActivity, Observer {
            val editor = sharedPreferences.edit()
            editor.putString("DevToken", it)
            editor.apply()
        })

        name.afterTextChanged {
            registrationViewModel.loginDataChanged(
                name.text.toString(),
                email.text.toString(),
                password.text.toString()
            )
        }

        email.afterTextChanged {
            registrationViewModel.loginDataChanged(
                name.text.toString(),
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                registrationViewModel.loginDataChanged(
                    name.text.toString(),
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        registrationViewModel.login(
                                email.text.toString(),
                                password.text.toString()
                        )
                }
                false
            }

            register.setOnClickListener {
                loading.visibility = View.VISIBLE
                registrationViewModel.register(name.text.toString(), email.text.toString(), password.text.toString())
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
        editor.putString("Email", regEmail.text.toString())
        editor.apply()

        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
