package hu.bme.aut.android.timetic.receiver

import android.accounts.Account
import android.accounts.AccountManager
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

// The authority for the sync adapter's content provider
const val AUTHORITY = "hu.bme.aut.android.timetic.syncAdapter"
// An account type, in the form of a domain name
const val ACCOUNT_TYPE = "hu.bme.aut.android.timetic"
// The account name
const val ACCOUNTPlaceholder = "placeholderaccount"

// Account
const val ACCOUNT = "default_account"
// Sync interval constants
const val SECONDS_PER_MINUTE = 60L
const val SYNC_INTERVAL_IN_MINUTES = 15L
const val SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        //TODO periodic or not?
        val mAccount = createSyncAccount(context)
        Log.d( "EZAZ", "alarm")
        // Get the content resolver for your app
        val mResolver = context.contentResolver
/*
        val settingsBundle = Bundle().apply {
            putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        }
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle)

 */

        ContentResolver.addPeriodicSync(
            mAccount,
            AUTHORITY,
            Bundle.EMPTY,
            86400)
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