package hu.bme.aut.android.timetic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.ui.loginAregistration.login.LoginActivity
import kotlinx.android.synthetic.main.activity_choose_organisation.*


class ChooseOrganisationActivity : AppCompatActivity() {
    private var organisationList: List<CommonOrganization>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_organisation)

        val backend =
            NetworkDeveloperInteractor(
                null,
                null
            )
        Log.d("EZAZ", "errrrrror")

        backend.getOrganisations(onSuccess = this::successList, onError = this::error)

        ChooseOrganisation.setOnItemClickListener { parent, view, position, id ->
            Log.d("EZAZ", "itemclicked")

            fabLoginChooseOrganisation.setOnClickListener {
                saveOrganisationUrl(organisationList?.get(position)?.serverUrl)

                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("OrganisationUrl", organisationList?.get(position)?.serverUrl)
                startActivity(intent)
            }
        }
    }

    private fun saveOrganisationUrl(serverUrl: String?) {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "secure_shared_preferences",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = secureSharedPreferences.edit()
        editor.putString("OrganisationUrl", serverUrl)
        editor.apply()
    }

    private fun successList(list: List<CommonOrganization>) {
        Log.d("EZAZ", "succcccess")

        organisationList = list

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.select_dialog_item, getOrganisationsName(list))

        ChooseOrganisation.threshold = 1

        ChooseOrganisation.setAdapter(adapter)
        //TODO
    }


    private fun error(e: Throwable) {
        Log.d("EZAZ", "errrrrror")
        //TODO
    }

    private fun getOrganisationsName(list: List<CommonOrganization>) : Array<String?> {
        val newList = arrayOfNulls<String>(list.size)
        for (item in list){
            newList[list.indexOf(item)] = item.name
        }
        return newList
    }
}