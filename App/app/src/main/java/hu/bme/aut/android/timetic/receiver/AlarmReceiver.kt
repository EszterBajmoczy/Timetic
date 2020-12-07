package hu.bme.aut.android.timetic.receiver

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import hu.bme.aut.android.timetic.R

// The authority for the sync adapter's content provider
const val AUTHORITY = "hu.bme.aut.android.timetic.syncAdapter"
// An account type, in the form of a domain name
const val ACCOUNT_TYPE = "hu.bme.aut.android.timetic"

// Account
const val ACCOUNT = "default_account"

class AlarmReceiver : BroadcastReceiver() {
    companion object{
        var count = 0
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("Title")
        val text = intent?.getStringExtra("Text")

        if(title != null){
            //if it's called just to make a notification of an appointment
            notification(title, text, context)
        } else {
            //if it's called to synchronize
            //TODO periodic or not?
            val mAccount = createSyncAccount(context)
            Log.d( "EZAZ", "alarm")

            notification( "AlarmReceiver ;)", context = context)
            // Get the content resolver for your app
            val mResolver = context.contentResolver

            val settingsBundle = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
            ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle)
/*
        ContentResolver.addPeriodicSync(
            mAccount,
            AUTHORITY,
            Bundle.EMPTY,
            SYNC_INTERVAL) */

        }
    }

    private fun createSyncAccount(context: Context): Account {
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        return Account(ACCOUNT, ACCOUNT_TYPE).also { }
    }

    private fun notification(title: String, text: String? = null, context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification =
            NotificationCompat.Builder(context, "default")
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo_round)
                .setContentTitle(title)
                .setContentText(text)
                .build()
        manager.notify(count, notification)
        count += 1
    }
}