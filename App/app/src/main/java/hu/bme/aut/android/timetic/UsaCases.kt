package hu.bme.aut.android.timetic

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object Singleton {

    fun logBackendError(e: Throwable, code: Int?, call: String){
        Log.d("EZAZ", "error")
        when(code) {
            400 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "400 - Bad Request")
            401 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "401 - Unauthorized ")
            403 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "403 - Forbidden")
            404 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "404 - Not Found")
            409 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "409 - Conflict")
        }
        FirebaseCrashlytics.getInstance().setCustomKey("Call", call)
        FirebaseCrashlytics.getInstance().recordException(e)
    }


}