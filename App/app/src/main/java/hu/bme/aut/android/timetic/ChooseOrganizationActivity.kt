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
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.ui.loginAregistration.login.LoginActivity
import kotlinx.android.synthetic.main.activity_choose_organization.*
import java.lang.Exception


class ChooseOrganizationActivity : AppCompatActivity() {
    private var organizationList: List<CommonOrganization>? = null
    private lateinit var viewModel: ChooseOrganizationViewModel

    private val internetStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                tNoInternetConnectionChooseOrganization.visibility = View.GONE
                initialize()
                context.unregisterReceiver(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_organization)

        viewModel = ViewModelProviders.of(this).get(ChooseOrganizationViewModel::class.java)

        //check internet connection
        val connectivityManager = applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if(isNetworkAvailable){
            initialize()
        } else {
            tNoInternetConnectionChooseOrganization.visibility = View.VISIBLE
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            applicationContext?.registerReceiver(internetStateChangedReceiver, intentFilter)
        }
    }

    private fun initialize() {
        //viewModel.init()
        viewModel.organizationList.observe(this, Observer{
            organizationList = it
            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.select_dialog_item, getOrganizationsName(it))

            ChooseOrganization.threshold = 1

            ChooseOrganization.setAdapter(adapter)
        })

        ChooseOrganization.setOnItemClickListener { parent, view, position, id ->
            fabLoginChooseOrganization.setOnClickListener {
                saveOrganizationUrl(organizationList?.get(position)?.serverUrl)

                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("OrganizationUrl", organizationList?.get(position)?.serverUrl)
                startActivity(intent)
            }
        }
    }

    private fun saveOrganizationUrl(serverUrl: String?) {
        val secureSharedPreferences = MyApplication.secureSharedPreferences

        val editor = secureSharedPreferences.edit()
        editor.putString("OrganizationUrl", serverUrl)
        editor.apply()
    }

    private fun getOrganizationsName(list: List<CommonOrganization>) : Array<String?> {
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