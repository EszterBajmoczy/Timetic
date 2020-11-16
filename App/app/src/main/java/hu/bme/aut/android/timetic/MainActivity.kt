package hu.bme.aut.android.timetic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.android.timetic.create.toHashMap
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.receiver.AlarmReceiver
import hu.bme.aut.android.timetic.settings.SettingsActivity
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModel
import hu.bme.aut.android.timetic.ui.calendar.CalendarViewModelFactory
import kotlinx.android.synthetic.main.nav_header_main.view.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: CalendarViewModel
    private lateinit var role: Role

    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.deleteAllFromProject()

            val secureSharedPreferences = MyApplication.secureSharedPreferences

            val editor = secureSharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@MainActivity, StartScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(logoutReceiver, IntentFilter("Logout"))

    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(logoutReceiver)
        } catch (e: Exception){

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val pref = MyApplication.secureSharedPreferences
        setFirstRunAndSynchronizeReceiver(pref)

        viewModel = ViewModelProvider(this, CalendarViewModelFactory()).get(CalendarViewModel::class.java)

        if(!MyApplication.getOrganisationUrl().isNullOrEmpty() || MyApplication.getOrganisationUrl() != ""){
            role = Role.EMPLOYEE
            viewModel.downloadAppointments(role, MyApplication.getOrganisationUrl()!!,
                MyApplication.getToken()!!)
        } else {
            role = Role.CLIENT
            val fab: FloatingActionButton = findViewById(R.id.fab)
            fab.visibility = View.GONE

            val organisationsMapString = MyApplication.secureSharedPreferences.getString("OrganisationsMap", "")
            val organisationMap = organisationsMapString!!.toHashMap()
            for((url,token) in organisationMap){
                viewModel.downloadAppointments(
                    role,
                    url,
                    token
                )
            }
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val menu = navView.menu
        //Customize menu to role
        if(MyApplication.getOrganisationUrl().isNullOrEmpty() || MyApplication.getOrganisationUrl() == ""){
            menu.removeItem(R.id.nav_statistic)
        } else {
            menu.removeItem(R.id.nav_organisation_operation)
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_calendar, R.id.nav_statistic, R.id.nav_client_operation, R.id.nav_organisation_operation, R.id.nav_log_out
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val header: View = navView.getHeaderView(0)
        val email = pref.getString("Email", "")
        header.tEmail.text = email
    }

    private fun setFirstRunAndSynchronizeReceiver(pref: SharedPreferences) {
        //if(!pref.contains("NotFirst")){
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putBoolean("NotFirst", false)
            editor.apply()

            val calAlarm = Calendar.getInstance()
            calAlarm[Calendar.HOUR_OF_DAY] = 15
            calAlarm[Calendar.MINUTE] = 32
            calAlarm[Calendar.SECOND] = 0

            val intent = Intent()
            intent.setClass(applicationContext, AlarmReceiver::class.java)
            intent.action = ".receiver.AlarmReceiver"
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager =  getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calAlarm.timeInMillis, pendingIntent)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calAlarm.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        Log.d( "EZAZ", "register alarm")
       // }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_settings){
            val intent = Intent(
                this@MainActivity,
                SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
