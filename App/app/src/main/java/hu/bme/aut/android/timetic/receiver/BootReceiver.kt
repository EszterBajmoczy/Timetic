package hu.bme.aut.android.timetic.receiver

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import hu.bme.aut.android.timetic.R
import java.util.*


class BootReceiver : BroadcastReceiver() {
    private lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent?) {
        this.context = context
        val calAlarm = Calendar.getInstance()
        calAlarm[Calendar.HOUR_OF_DAY] = 0
        calAlarm[Calendar.MINUTE] = 56
        calAlarm[Calendar.SECOND] = 0

        val intent = Intent()
        intent.setClass(context, AlarmReceiver::class.java)
        intent.action = ".receiver.AlarmReceiver"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager =  context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calAlarm.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        Log.d( "EZAZ", "boot receiver")

        notification( "Boot")
    }

    private fun notification(title: String, text: String? = null) {
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
        manager.notify(1, notification)
    }
}