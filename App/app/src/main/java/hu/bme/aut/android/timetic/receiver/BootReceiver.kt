package hu.bme.aut.android.timetic.receiver

import android.accounts.Account
import android.accounts.AccountManager
import android.app.*
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import java.util.*

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        firstSync(context)

        //set daily synchronization at 00.15
        val calAlarm = Calendar.getInstance()
        calAlarm[Calendar.HOUR_OF_DAY] = 0
        calAlarm[Calendar.MINUTE] = 15
        calAlarm[Calendar.SECOND] = 0
        Log.d("TIMETIC_LOG", "BootReceiver")
        val intent = Intent()
        intent.setClass(context, AlarmReceiver::class.java)
        intent.action = ".receiver.AlarmReceiver"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager =  context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calAlarm.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    private fun firstSync(context: Context) {
        val mAccount = createSyncAccount(context)

        val settingsBundle = Bundle().apply {
            putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        }
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle)
    }

    private fun createSyncAccount(context: Context): Account {
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        return Account(ACCOUNT, ACCOUNT_TYPE).also { newAccount ->
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {
                /*
                 * If you don't set android:syncable="true" in
                 * in your <provider> element in the manifest,
                 * then call context.setIsSyncable(account, AUTHORITY, 1)
                 * here.
                 */
            } else {
                /*
                 * The account exists or some other error occurred. Log this, report it,
                 * or handle it internally.
                 */
            }
        }
    }
}