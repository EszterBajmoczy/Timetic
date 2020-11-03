package hu.bme.aut.android.timetic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hu.bme.aut.android.timetic.ui.loginAregistration.login.LoginActivity
import hu.bme.aut.android.timetic.ui.loginAregistration.registration.RegistrationActivity
import kotlinx.android.synthetic.main.activity_start_screen.*

class StartScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        btSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btRegistration.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        btOrganisationSignIn.setOnClickListener {
            val intent = Intent(this, ChooseOrganisationActivity::class.java)
            startActivity(intent)
        }
    }
}
