package hu.bme.aut.android.timetic.ui.organizationoperations.info

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.recyclerViewAdapter.ClientAdapter
import hu.bme.aut.android.timetic.create.NewClientActivity
import hu.bme.aut.android.timetic.create.toHashMap
import hu.bme.aut.android.timetic.data.model.Person
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
                }
            }
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
        startActivity(Intent.createChooser(intent, "Choose an Email client :"))
    }
}