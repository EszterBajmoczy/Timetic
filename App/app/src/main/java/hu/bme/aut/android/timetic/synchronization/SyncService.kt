package hu.bme.aut.android.timetic.synchronization

import android.app.Service
import android.content.Intent
import android.os.IBinder

/*
* This class based on the tutorial of developer.android.com/training/sync-adapters
 */
class SyncService : Service() {
    companion object {
        private var sSyncAdapter: SyncAdapter? = null
        private val sSyncAdapterLock = Any()
    }

    override fun onCreate() {
        synchronized(sSyncAdapterLock) {
            sSyncAdapter = sSyncAdapter ?: SyncAdapter(applicationContext, true)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return sSyncAdapter?.syncAdapterBinder ?: throw IllegalStateException()
    }
}