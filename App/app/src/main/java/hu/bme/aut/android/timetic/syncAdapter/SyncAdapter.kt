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
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.*
import hu.bme.aut.android.timetic.create.getAppointment
import hu.bme.aut.android.timetic.create.getClient
import hu.bme.aut.android.timetic.create.getEmployee
import hu.bme.aut.android.timetic.create.toHashMap
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import hu.bme.aut.android.timetic.network.apiOrganization.OrganizationApi
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import hu.bme.aut.android.timetic.network.models.CommonAppointment
import hu.bme.aut.android.timetic.network.models.ForClientAppointment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    val mContentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private var mapSize: Int = 0
    private var appointments = ArrayList<Appointment>()
    private var persons = ArrayList<Person>()
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

        if(MyApplication.getToken().isNullOrEmpty() || MyApplication.getDevToken().isNullOrEmpty() ){
            role = if(MyApplication.getToken() != null && MyApplication.getToken()!!.isNotEmpty()){
                Role.EMPLOYEE
            } else {
                Role.CLIENT
            }
            synchronize()
        } else {
            notification( "Unable to synchronize, please log in")
        }
    }

    private fun synchronize() {
        when(role) {
            Role.EMPLOYEE -> {
                //sync
                val apiOrg = getApi(MyApplication.getOrganizationUrl()!!)
                val response: retrofit2.Response<List<CommonAppointment>> = apiOrg.employeeAppointmentsGet().execute()
                if (response.isSuccessful){
                    successAppointmentList(response.body()!!)
                } else {
                    notification( "Unable to synchronize")
                }
                //async
                //val backend =  NetworkOrganizationInteractor(MyApplication.getOrganizationUrl()!!, null, HttpBearerAuth("bearer", MyApplication.getToken()!!))
                //backend.getEmployeeAppointments(onSuccess = this::successAppointmentList, onError = this::errorAppointmentList)
            }
            Role.CLIENT -> {
                val organizationsMapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
                val organizationMap = organizationsMapString!!.toHashMap()
                mapSize = organizationMap.size
                for((url, _) in organizationMap){
                    //sync
                    val apiOrg = getApi(url)
                    val response: retrofit2.Response<List<ForClientAppointment>> = apiOrg.clientAppointmentsGet().execute()
                    if (response.isSuccessful){
                        successClientAppointmentList(response.body()!!, url)
                    } else {
                        notification( "Unable to synchronize")
                    }
                    /*
                    //async
                    val backend = NetworkOrganizationInteractor(
                        url,
                        null,
                        HttpBearerAuth(
                            "bearer",
                            token
                        )
                    )
                    backend.getClientAppointments(onSuccess = this::successClientAppointmentList, onError = this::errorAppointmentList)

                     */
                }
            }
        }
    }

    private fun getApi(url: String): OrganizationApi {
        val m = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val client =  OkHttpClient.Builder()
            .addInterceptor(HttpBearerAuth(
                "bearer",
                MyApplication.getToken()!!
            ))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(m))
            .build()

        return retrofit.create(OrganizationApi::class.java)
    }

    private fun successClientAppointmentList(list: List<ForClientAppointment>, organizationUrl: String) {
        mapSize -= 1
        for(item in list){
            val a = item.getAppointment(organizationUrl)
            appointments.add(a)
            val c = item.getEmployee()
            if(!persons.contains(c)){
                persons.add(c)
            }
        }
        if(mapSize < 1){
            appointments()
            clients()
        }
    }

    private fun successAppointmentList(list: List<CommonAppointment>) {
        for(item in list){
            appointments.add(item.getAppointment())
            val c = item.getClient()
            if(c != null && !persons.contains(c)) {
                persons.add(c)
            }
        }
        appointments()
        clients()
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
        Log.d("EZAZ", "appontments success")
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)
        val apps = repo.getAppointmentList()

        UseCases().appointmentOrganizer(repo, CoroutineScope(Dispatchers.IO), appointments, apps)

        //check if there is an appointment today
        for(item in appointments) {
            val todayStart = Calendar.getInstance()
            todayStart.set(Calendar.HOUR_OF_DAY, 0)
            todayStart.set(Calendar.MINUTE, 0)
            todayStart.set(Calendar.SECOND, 0)
            val todayEnd = Calendar.getInstance()
            todayEnd.set(Calendar.HOUR_OF_DAY, 23)
            todayEnd.set(Calendar.MINUTE, 59)
            todayEnd.set(Calendar.SECOND, 59)

            if(item.start_date > todayStart && item.start_date < todayEnd){
                val title = getTitle(item)
                val text = getText(item)
                notification(title, text)
            }
        }
    }

    private fun clients() {
        val dao = MyApplication.myDatabase.roomDao()
        repo = DBRepository(dao)
        val personsFromDB = repo.getPersonList()

        UseCases().personOrganizer(repo, CoroutineScope(Dispatchers.IO), persons, personsFromDB)
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
}