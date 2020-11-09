package hu.bme.aut.android.timetic.syncAdapter

import android.accounts.Account
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.facebook.react.bridge.UiThreadUtil
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.create.getAppointment
import hu.bme.aut.android.timetic.create.getClient
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganisationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.CommonClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    val mContentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private var notificationCount = 0
    private lateinit var repo: DBRepository

    override fun onPerformSync(
        account: Account,
        extras: Bundle,
        authority: String,
        provider: ContentProviderClient,
        syncResult: SyncResult
    ) {
        notification( "SyncAdapter ;)")
        Log.d( "EZAZ", "syncadapter")
        if(MyApplication.getToken() == null){
            notification( "Unable to synchronize, please log in")
        }

        //TODO Main thread?
        GlobalScope.launch(Dispatchers.Main) {
            synchronize()
        }

    }

    private fun synchronize() {
        //TODO role
        val backend =  NetworkOrganisationInteractor(MyApplication.getOrganisationUrl()!!, null, HttpBearerAuth("bearer", MyApplication.getToken()!!))
        backend.getAppointments(onSuccess = this::successAppointmentList, onError = this::errorAppointmentList)
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
        manager.notify(notificationCount++, notification)
    }

    private fun successAppointmentList(list: List<CommonAppointment>) {
        Log.d("EZAZ", "appontments success")

        GlobalScope.launch {
            val dao = MyApplication.myDatabase.roomDao()
            repo = DBRepository(dao)
            val apps = repo.getAppointmentList()
            val clients = repo.getClientList()

            val appointmentIds = ArrayList<String>()
            val checkedClients = ArrayList<CommonClient>()

            //check if it is already in the local database
            for (item in list){
                appointmentIds.add(item.id!!)

                val start = Calendar.getInstance()
                start.timeInMillis = item.startTime!!
                val end = Calendar.getInstance()
                end.timeInMillis = item.endTime!!

                if(newOrUpdatedAppointment(item, apps)){
                    val a = item.getAppointment()
                    if(!item.isPrivate!!){
                        val c = item.getClient()
                        if(c != null && newOrUpdatedClient(item.client!!, clients) && !checkedClients.contains(item.client)){
                            checkedClients.add(item.client)
                            insert(c)
                        }
                    }
                    insert(a)
                }
                val todayStart = Calendar.getInstance()
                todayStart.set(Calendar.HOUR_OF_DAY, 0)
                todayStart.set(Calendar.MINUTE, 0)
                todayStart.set(Calendar.SECOND, 0)
                val todayEnd = Calendar.getInstance()
                todayEnd.set(Calendar.HOUR_OF_DAY, 23)
                todayEnd.set(Calendar.MINUTE, 59)
                todayEnd.set(Calendar.SECOND, 59)

                if(item.startTime > todayStart.timeInMillis && item.startTime < todayEnd.timeInMillis){
                    val title = getTitle(item.getAppointment())
                    val text = getText(item.getAppointment())
                    notification(title, text)
                }
            }
            deleteCanceledAppointments(appointmentIds, apps)
        }
    }

    private fun getTitle(appointment: Appointment): String {
        return if(!appointment.private_appointment && appointment.activity != null){
            appointment.activity
        } else if(appointment.note != "" && appointment.note != null){
            appointment.note
        } else{
            "You have an appointment today:"
        }
    }

    private fun getText(appointment: Appointment): String {
        return "${format(appointment.start_date.get(Calendar.HOUR_OF_DAY))}:${format(appointment.start_date.get(Calendar.MINUTE))} " +
                "- ${format(appointment.end_date.get(Calendar.HOUR_OF_DAY))}:${format(appointment.end_date.get(Calendar.MINUTE))}"
    }

    private fun format(value: Int) : String{
        if(value.toString().length == 1){
            return "${0}${value}"
        }
        return value.toString()
    }

    //delete if some Appointment was deleted at the server side
    private fun deleteCanceledAppointments(
        ids: List<String>,
        apps: List<Appointment>
    ) {
        for(item in apps){
            if(!ids.contains(item.netId)){
                delete(item)
            }
        }
    }

    //checks if the appointment already saved
    private fun newOrUpdatedAppointment(
        appointment: CommonAppointment,
        apps: List<Appointment>
    ) : Boolean {
        for(item in apps){
            if(item.netId == appointment.id){
                if(item.note == appointment.note && item.start_date.timeInMillis == appointment.startTime &&
                    item.end_date.timeInMillis == appointment.endTime &&
                    item.private_appointment == appointment.isPrivate &&
                    item.address == appointment.place && appointment.isPrivate){
                    return false
                }
                if(item.note == appointment.note && item.start_date.timeInMillis == appointment.startTime &&
                    item.end_date.timeInMillis == appointment.endTime && item.price == appointment.price &&
                    item.private_appointment == appointment.isPrivate && item.videochat == appointment.online &&
                    item.address == appointment.place && item.client == appointment.client!!.name && item.activity == appointment.activity!!.name){
                    return false
                }
                else if(item.note == appointment.note && item.start_date.timeInMillis == appointment.startTime &&
                    item.end_date.timeInMillis == appointment.endTime &&
                    item.private_appointment == appointment.isPrivate &&
                    item.address == appointment.place){
                    return false
                }
                delete(item)
                return true
            }
        }
        return true
    }

    //checks if the client already saved
    private fun newOrUpdatedClient(
        client: CommonClient,
        clients: List<Client>
    ) : Boolean {
        for(item in clients){
            if(item.netId == client.id){
                if(item.name == client.name && item.email == client.email && item.phone == client.phone ){
                    return false
                }
                delete(item)
                return true
            }
        }
        return true
    }

    private fun insert(appointment: Appointment) = GlobalScope.launch {
        repo.insert(appointment)
    }

    private fun insert(client: Client) = GlobalScope.launch {
        repo.insert(client)
    }

    private fun delete(appointment: Appointment) = GlobalScope.launch {
        repo.deleteAppointment(appointment)
    }

    private fun delete(client: Client) = GlobalScope.launch {
        repo.deleteClient(client)
    }

    private fun errorAppointmentList(e: Throwable) {
        notification("Unable to synchronize, please log in")
        //TODO
    }
}