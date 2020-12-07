package hu.bme.aut.android.timetic.views_viewmodels.newclient

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.Role
import hu.bme.aut.android.timetic.network.models.CommonClient
import kotlinx.android.synthetic.main.activity_new_client.*
import kotlinx.android.synthetic.main.item_new_client.view.*


class NewClientActivity : AppCompatActivity() {
    private lateinit var viewModel: NewClientViewModel
    private lateinit var role: Role
    private var ids = ArrayList<Int>()
    private var infos = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_client)

        val personalInfo = intent.getStringArrayExtra("PersonalInfos")
        val organizationUrl = intent.getStringExtra("OrganizationUrl")
        val organizationId = intent.getStringExtra("OrganizationId")

        viewModel = ViewModelProviders.of(this).get(NewClientViewModel::class.java)

        if(organizationId != null && organizationUrl != null) {
            role = Role.CLIENT
            setTitle(R.string.registration)

            viewModel.initialize(organizationUrl)

            val pref = MyApplication.secureSharedPreferences
            etClientName.setText(pref.getString("UserName", ""))
            etClientEmail.setText(pref.getString("Email", ""))

            val mInflater = LayoutInflater.from(applicationContext)
            val mDynamicLayoutsContainer = findViewById<LinearLayout>(R.id.tableNewClient)

            personalInfo?.let {
                for(item in personalInfo){
                    val firstI = View.generateViewId()
                    val secondI = View.generateViewId()

                    infos.add(item)
                    ids.add(secondI)

                    val itemRow = mInflater.inflate(R.layout.item_new_client, null, false)
                    //text field
                    itemRow.textNewClient.text = item
                    itemRow.textNewClient.id = firstI
                    //editText field
                    itemRow.editTextNewClient.id = secondI

                    mDynamicLayoutsContainer.addView(itemRow)
                }
            }
        } else {
            role = Role.EMPLOYEE
            setTitle(R.string.registrate_new_client)

            viewModel.initialize(MyApplication.getOrganizationUrl()!!, MyApplication.getToken()!!)

            viewModel.data.observe(this, Observer { list ->
                val infoList = list.clientPersonalInfoFields

                infoList?.let {
                    val mInflater = LayoutInflater.from(applicationContext)
                    val mDynamicLayoutsContainer = findViewById<LinearLayout>(R.id.tableNewClient)

                    for(item in it){
                        val firstI = View.generateViewId()
                        val secondI = View.generateViewId()

                        infos.add(item)
                        ids.add(secondI)

                        val itemRow = mInflater.inflate(R.layout.item_new_client, null, false)
                        //text field
                        itemRow.textNewClient.text = item
                        itemRow.textNewClient.id = firstI
                        //editText field
                        itemRow.editTextNewClient.id = secondI

                        mDynamicLayoutsContainer.addView(itemRow)
                    }
                }
            })
        }

        btSaveClient.setOnClickListener {
            if(check()){
                val c = getClient()
                if(isNetworkAvailable()){
                    when(role) {
                        Role.EMPLOYEE -> viewModel.addClient(c)
                        Role.CLIENT -> viewModel.registerClient(c)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.network_needed), Toast.LENGTH_LONG).show()
                }
            }
            else{
                Toast.makeText(this, getString(R.string.fill_all_fields_error), Toast.LENGTH_LONG).show()
            }
        }

        btCancelClient.setOnClickListener {
            finish()
        }

        viewModel.success.observe(this, Observer {
            when(role) {
                Role.EMPLOYEE -> finish()
                Role.CLIENT -> {
                    viewModel.getTokenForClient(organizationUrl, MyApplication.getEmail()!!, MyApplication.getRefreshToken()!!)
                }
            }
        })

        viewModel.tokenForClient.observe(this, Observer {
            val mapString = MyApplication.secureSharedPreferences.getString("OrganizationsMap", "")
            val map = mapString!!.toHashMap()
            map[organizationUrl!!] = it

            val editor = MyApplication.secureSharedPreferences.edit()
            editor.putString("OrganizationsMap", map.toString())
            editor.apply()

            viewModel.sendOrganizationIdToDev(MyApplication.getDevToken()!!, organizationId!!)
        })

        viewModel.clientRegistered.observe(this, Observer {
            when(it) {
                true -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                false -> Toast.makeText(applicationContext, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun check() : Boolean {
        for((index) in infos.withIndex()){
            val view = findViewById<EditText>(ids[index])
            if(view.text.toString() == ""){
                return false
            }
        }
        if(etClientName.text.toString() == "" || etClientEmail.text.toString() == "" || etClientPhone.text.toString() == ""){
            return false
        }
        return true
    }

    private fun getClient(): CommonClient{
        val personalInfos = HashMap<String, String>()
        for((index, item) in infos.withIndex()){
            val view = findViewById<EditText>(ids[index])
            personalInfos[item] = view.text.toString()
        }
        return CommonClient(
            name = etClientName.text.toString(),
            email = etClientEmail.text.toString(),
            phone = etClientPhone.text.toString(),
            personalInfos = personalInfos
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun String.toHashMap(): HashMap<String, String> {
    val map = HashMap<String, String>()
    var tmp = this.substringAfter('{').substringBefore('}')
    while(tmp.contains(", ") || tmp.isNotEmpty()) {
        val key = tmp.substringBefore('=')
        tmp = tmp.substringAfter('=')
        val value: String
        if(tmp.contains(", ")){
            value = tmp.substringBefore(", ")
            tmp = tmp.substringAfter(", ")
        } else{
            value = tmp
            tmp = ""
        }
        map[key] = value
    }
    return map
}