package hu.bme.aut.android.timetic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.ui.clientoperations.ClientOperationsViewModel
import hu.bme.aut.android.timetic.ui.loginAregistration.login.LoginActivity
import kotlinx.android.synthetic.main.activity_choose_organisation.*


class ChooseOrganisationActivity : AppCompatActivity() {
    private var organisationList: List<CommonOrganization>? = null
    private lateinit var viewModel: ChooseOrganisationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_organisation)

        viewModel = ViewModelProviders.of(this).get(ChooseOrganisationViewModel::class.java)

        viewModel.organisationList.observe(this, Observer{
            organisationList = it
            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.select_dialog_item, getOrganisationsName(it))

            ChooseOrganisation.threshold = 1

            ChooseOrganisation.setAdapter(adapter)
        })

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
        val secureSharedPreferences = MyApplication.secureSharedPreferences

        val editor = secureSharedPreferences.edit()
        editor.putString("OrganisationUrl", serverUrl)
        editor.apply()
    }

    private fun getOrganisationsName(list: List<CommonOrganization>) : Array<String?> {
        val newList = arrayOfNulls<String>(list.size)
        for (item in list){
            newList[list.indexOf(item)] = item.name
        }
        return newList
    }
}