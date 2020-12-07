package hu.bme.aut.android.timetic.synchronization

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService : Service() {
    private lateinit var mAuthenticator: StubAuthenticator

    override fun onCreate() {
        // Create a new authenticator object
        mAuthenticator = StubAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder = mAuthenticator.iBinder
}