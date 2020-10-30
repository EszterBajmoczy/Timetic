package hu.bme.aut.android.timetic.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import hu.bme.aut.android.timetic.R


class SettingsActivity : AppCompatActivity() {
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)


        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "secure_shared_preferences",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        //TODO viewmodelfactory?
        viewModel = SettingsViewModel()
        viewModel.getDataForAppointmentCreation(secureSharedPreferences.getString("OrganisationUrl", "").toString(),
            secureSharedPreferences.getString("Token", "").toString())

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment(viewModel)
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment(private val viewModel: SettingsViewModel) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val activityType = findPreference<ListPreference>("activityType")

            viewModel.activities.observe(this, androidx.lifecycle.Observer {
                val entryValues = ArrayList<String>()

                if (activityType != null) {
                    activityType.entries = it.toTypedArray()
                    activityType.setDefaultValue(it[0])
                    activityType.entryValues = it.toTypedArray()
                }
            })
        }
    }

}