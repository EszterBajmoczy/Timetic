package hu.bme.aut.android.timetic.settings

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R


class SettingsActivity : AppCompatActivity() {
    private var viewModel: SettingsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        //TODO viewmodelfactory?
        if(!MyApplication.getOrganisationUrl().isNullOrEmpty() && MyApplication.getOrganisationUrl() != "") {
            viewModel = SettingsViewModel()
            viewModel!!.getDataForAppointmentCreation(MyApplication.getOrganisationUrl()!!, MyApplication.getToken()!!)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment(viewModel)
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment(private val viewModel: SettingsViewModel?) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            if(viewModel == null){
                val defaultVideoChat = findPreference<SwitchPreference>("defaultVideoChat")
                defaultVideoChat?.isVisible = false

                val timeRange = findPreference<EditTextPreference>("timeRange")
                timeRange?.isVisible = false

                val price = findPreference<EditTextPreference>("price")
                price?.isVisible = false

                val activityType = findPreference<ListPreference>("activityType")
                activityType?.isVisible = false

            } else {
                val activityType = findPreference<ListPreference>("activityType")

                if(isNetworkConnection()){
                    viewModel.activities.observe(this, androidx.lifecycle.Observer {
                        if (activityType != null) {
                            activityType.entries = it.toTypedArray()
                            activityType.setDefaultValue(it[0])
                            activityType.entryValues = it.toTypedArray()
                        }
                    })
                } else {
                    activityType?.isEnabled = false
                    Toast.makeText(context, "Nincs internet kapcsolat, a tevékenység beállításához kapcsolja be.", Toast.LENGTH_LONG).show()
                }
            }
        }
        private fun isNetworkConnection(): Boolean {
            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}