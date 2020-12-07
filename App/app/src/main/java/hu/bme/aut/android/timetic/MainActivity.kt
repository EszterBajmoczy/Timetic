package hu.bme.aut.android.timetic

import android.content.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.android.timetic.create.toHashMap
import hu.bme.aut.android.timetic.receiver.BootReceiver
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

        if(!MyApplication.getOrganizationUrl().isNullOrEmpty() || MyApplication.getOrganizationUrl() != ""){
            role = Role.EMPLOYEE
            viewModel.downloadAppointments(role, MyApplication.getOrganizationUrl()!!,
                MyApplication.getToken()!!)
        } else {
            role = Role.CLIENT
            val fab: FloatingActionButton = findViewById(R.id.fab)
            fab.visibility = View.GONE

            val organizationsMapString = pref.getString("OrganizationsMap", "")
            val organizationMap = organizationsMapString!!.toHashMap()
            for((url,token) in organizationMap){
                viewModel.downloadAppointments(
                    role,
                    url,
                    token
                )
            }
        }

        //update last synchronization date
        viewModel.appsDownloaded.observe(this, androidx.lifecycle.Observer {
            val calendar = Calendar.getInstance()
            val editor = pref.edit()
            editor.putLong("LastSync", calendar.timeInMillis)
            editor.apply()
        })

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val menu = navView.menu
        //Customize menu to role
        if(MyApplication.getOrganizationUrl().isNullOrEmpty() || MyApplication.getOrganizationUrl() == ""){
            menu.removeItem(R.id.nav_statistic)
        } else {
            menu.removeItem(R.id.nav_organization_operation)
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_calendar, R.id.nav_statistic, R.id.nav_client_operation, R.id.nav_organization_operation, R.id.nav_log_out
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val header: View = navView.getHeaderView(0)
        val name = pref.getString("UserName", "")
        val email = pref.getString("Email", "")
        header.tName.text = name
        header.tEmail.text = email
    }

    private fun setFirstRunAndSynchronizeReceiver(pref: SharedPreferences) {
        //if(!pref.contains("NotFirst")){
            val editor = pref.edit()
            editor.putBoolean("NotFirst", true)
            editor.apply()

            val intent = Intent()
            intent.setClass(applicationContext, BootReceiver::class.java)
            //set the synchronization
            BootReceiver().onReceive(applicationContext, intent)
        //}
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
