package hu.bme.aut.android.timetic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.dataManager.NetworkDeveloperInteractor
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.ui.clientoperations.ClientOperationsViewModel
import hu.bme.aut.android.timetic.ui.loginAregistration.login.LoginActivity
import kotlinx.android.synthetic.main.activity_choose_organisation.*
import kotlinx.android.synthetic.main.fragment_statistic_main.*
import java.lang.Exception


class ChooseOrganisationActivity : AppCompatActivity() {
    private var organisationList: List<CommonOrganization>? = null
    private lateinit var viewModel: ChooseOrganisationViewModel

    private val internetStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                tNoInternetConnectionChooseOrganisation.visibility = View.GONE
                initialize()
                context.unregisterReceiver(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_organisation)

        viewModel = ViewModelProviders.of(this).get(ChooseOrganisationViewModel::class.java)

        //check internet connection
        val connectivityManager = applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if(isNetworkAvailable){
            initialize()
        } else {
            tNoInternetConnectionChooseOrganisation.visibility = View.VISIBLE
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            applicationContext?.registerReceiver(internetStateChangedReceiver, intentFilter)
        }
    }

    private fun initialize() {
        //viewModel.init()
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

    override fun onPause() {
        try {
            applicationContext?.unregisterReceiver(internetStateChangedReceiver)
        } catch (e: Exception) {

        }
        super.onPause()
    }
}