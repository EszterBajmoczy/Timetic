package hu.bme.aut.android.timetic.syncAdapter

import android.accounts.Account
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.app.NotificationCompat
import hu.bme.aut.android.timetic.*
import hu.bme.aut.android.timetic.create.getAppointment
import hu.bme.aut.android.timetic.create.getClient
import hu.bme.aut.android.timetic.create.getEmployee
import hu.bme.aut.android.timetic.create.toHashMap
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.dataManager.NetworkOrganizationInteractor
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.ForClientAppointment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    val mContentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private var mapSize: Int = 0
    private var appointmentsBackend = ArrayList<Appointment>()
    private var personsBackend = ArrayList<Person>()
    private var notificationCount = 1
    private lateinit var repo: DBRepository
    private lateinit var role: Role

    override fun onPerformSync(
        account: Account,
        extras: Bundle,
        authority: String,
        provider: ContentProviderClient,
        syncResult: SyncResult
    ) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        notification( "SyncAdapter at $hour:$minute")

        //TODO save LastSync date in millis
        val calendar = Calendar.getInstance()
        val editor =  MyApplication.secureSharedPreferences.edit()
        editor.putLong("LastSync", calendar.timeInMillis)
        editor.apply()

        if(MyApplication.getToken().isNullOrEmpty() || MyApplication.getDevToken().isNullOrEmpty() ){
            role = if(MyApplication.getToken() != null && MyApplication.getToken()!!.isNotEmpty()){
                Role.EMPLOYEE
            } else {
                Role.CLIENT
            }
            Looper.prepare()
            synchronize()
        } else {
            notification( "Unable to synchronize, please log in")
        }
    }

    private fun synchronize() {
        when(role) {
            Role.EMPLOYEE -> {
                val backend =  NetworkOrganizationInteractor(MyApplication.getOrganizationUrl()!!, null, HttpBearerAuth("bearer", MyApplication.getToken()!!))
                backend.getEmployeeAppointments(onSuccess = this::successEmployeeAppointmentList, onError = this::errorAppointmentList)
            }
            Role.CLIENT -> {
                val organizationsMapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
                val organizationMap = organizationsMapString!!.toHashMap()
                mapSize = organizationMap.size
                for((url,token) in organizationMap){
                    val backend = NetworkOrganizationInteractor(
                        url,
                        null,
                        HttpBearerAuth(
                            "bearer",
                            token
                        )
                    )
                    backend.getClientAppointments(onSuccess = this::successClientAppointmentList, onError = this::errorAppointmentList)
                }
            }

        }

    }

    private fun successClientAppointmentList(list: List<ForClientAppointment>, organizationUrl: String) {
        mapSize -= 1
        for(item in list){
            val a = item.getAppointment(organizationUrl)
            appointmentsBackend.add(a)
            val c = item.getEmployee()
            if(!personsBackend.contains(c)){
                personsBackend.add(c)
            }
        }
        if(mapSize < 1){
            appointments()
            persons()
        }
    }

    private fun successEmployeeAppointmentList(list: List<CommonAppointment>) {
        for(item in list){
            appointmentsBackend.add(item.getAppointment())
            val c = item.getClient()
            if(c != null && !personsBackend.contains(c)) {
                personsBackend.add(c)
            }
        }
        appointments()
        persons()
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

    private fun appointments() {
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)
        //get appointments synchron from db
        val dbList = repo.getAppointmentList()

        val appointmentIds = ArrayList<String>()

        //check if it is already in the local database
        UseCases().appointmentOrganizer(repo, CoroutineScope(Dispatchers.IO), appointmentsBackend, dbList)

        for (item in appointmentsBackend) {
            //make a unique id in the list
            appointmentIds.add(item.backendId + item.organizationUrl)
        }

    }

    private fun persons() {
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)
        //get persons synchron from db
        val dbList = repo.getPersonList()

        //check if it is already in the local database
        UseCases().personOrganizer(repo, CoroutineScope(Dispatchers.IO), personsBackend, dbList)
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

    private fun errorAppointmentList(e: Throwable, code: Int?, call: String) {
        notification("Unable to synchronize, please log in")
        UseCases.logBackendError(e, code, call)
    }
}