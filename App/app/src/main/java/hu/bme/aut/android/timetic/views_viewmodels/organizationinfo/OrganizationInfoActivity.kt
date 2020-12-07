package hu.bme.aut.android.timetic.views_viewmodels.organizationinfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.StartScreenActivity
import hu.bme.aut.android.timetic.recyclerViewAdapter.ClientAdapter
import hu.bme.aut.android.timetic.views_viewmodels.newclient.NewClientActivity
import hu.bme.aut.android.timetic.views_viewmodels.newclient.toHashMap
import hu.bme.aut.android.timetic.models.Person
import hu.bme.aut.android.timetic.network.models.CommonEmployee
import kotlinx.android.synthetic.main.activity_organization_info.*

class OrganizationInfoActivity : AppCompatActivity() {
    private lateinit var viewModel: OrganizationInfoViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_info)

        val organizationUrl = intent.getStringExtra("OrganizationUrl")
        val organizationId = intent.getStringExtra("OrganizationId")

        viewModel = ViewModelProviders.of(this).get(OrganizationInfoViewModel::class.java)

        viewModel.getInformation(organizationUrl!!, MyApplication.getEmail()!!)
        viewModel.info.observe(this, Observer {info ->
            title = info.name
            OrganizationDetail.text = info.details
            adapter.update(getEmployees(info.employees!!))

            if(!isOrganizationsAppointmentsAlreadyActivated(organizationUrl)) {
                btShareData.visibility = View.VISIBLE
            } else if(info.isClientRegistered!!) {
                btShareData.setText(R.string.btActivateOrganization)
            }

            btShareData.setOnClickListener {
                if(!info.isClientRegistered!!) {
                    val intent = Intent(this, NewClientActivity::class.java)
                    intent.putExtra("PersonalInfos", info.clientPersonalInfoFields?.toTypedArray())
                    intent.putExtra("OrganizationUrl", organizationUrl)
                    intent.putExtra("OrganizationId", organizationId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, getString(R.string.activated_organization), Toast.LENGTH_LONG).show()
                    viewModel.sendOrganizationIdToDev(MyApplication.getDevToken()!!, organizationId)
                }
            }
        })

        viewModel.clientRegistered.observe(this, Observer {success ->
            if(success) {
                viewModel.getTokenForClient(organizationUrl, MyApplication.getEmail()!!, MyApplication.getRefreshToken()!!)
            } else {
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show()
            }
        })

        viewModel.tokenSuccess.observe(this, Observer {list ->
            val organizationsMapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
            val organizationMap = organizationsMapString!!.toHashMap()

            organizationMap[list[0]] = list[1]

            val editor = MyApplication.secureSharedPreferences.edit()
            editor.putString("OrganizationsMap", organizationMap.toString())
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        initRecyclerView()
    }

    private fun isOrganizationsAppointmentsAlreadyActivated(organizationUrl: String): Boolean{
        val mapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
        val organizationMap = mapString?.toHashMap()
        organizationMap?.let { map ->
            if(map.containsKey(organizationUrl)){
                return true
            }
        }
        return false
    }

    private fun getEmployees(employees: List<CommonEmployee>): List<Person> {
        val list = ArrayList<Person>()
        for(item in employees){
            val c = Person(null, item.id!!, item.name!!, item.email!!, item.phone!!)
            list.add(c)
        }
        return list
    }

    private fun initRecyclerView() {
        recyclerView = OrganizationInfoRecyclerView
        adapter = ClientAdapter(this::callCallBack, this::emailCallBack)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = adapter
    }

    private fun callCallBack(number: String){
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }

    private fun emailCallBack(email: String){
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        startActivity(Intent.createChooser(intent, getString(R.string.email_text)))
    }

    //handle system logout
    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.deleteAllFromProject()

            val secureSharedPreferences = MyApplication.secureSharedPreferences

            val editor = secureSharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@OrganizationInfoActivity, StartScreenActivity::class.java)
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
        } catch (e: Exception){}
    }
}