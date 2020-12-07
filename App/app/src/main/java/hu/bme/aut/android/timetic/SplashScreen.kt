package hu.bme.aut.android.timetic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    private val timeout: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val handler = Handler()
        val thread = Runnable {
            val secureSharedPreferences = MyApplication.secureSharedPreferences

            //checks if the user is already signed in
            if(secureSharedPreferences.contains("Token") || secureSharedPreferences.contains("DevToken")){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else{
                val intent = Intent(this@SplashScreen, StartScreenActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }
        handler.postDelayed(thread, timeout)
    }
}
