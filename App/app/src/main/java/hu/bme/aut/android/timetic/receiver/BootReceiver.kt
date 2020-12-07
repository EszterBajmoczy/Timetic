package hu.bme.aut.android.timetic.receiver

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class BootReceiver : BroadcastReceiver() {
    private lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent?) {
        this.context = context
        val calAlarm = Calendar.getInstance()
        calAlarm.add(Calendar.DAY_OF_MONTH, 1)
        calAlarm[Calendar.HOUR_OF_DAY] = 0
        calAlarm[Calendar.MINUTE] = 15
        calAlarm[Calendar.SECOND] = 0

        val intent = Intent()
        intent.setClass(context, AlarmReceiver::class.java)
        intent.action = ".receiver.AlarmReceiver"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager =  context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calAlarm.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }
}