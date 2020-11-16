package hu.bme.aut.android.timetic.ui.organisationoperations.info

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.adapter.ClientAdapter
import hu.bme.aut.android.timetic.create.NewClientActivity
import hu.bme.aut.android.timetic.create.toHashMap
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.network.models.CommonEmployee
import kotlinx.android.synthetic.main.activity_organisation_info.*

class OrganisationInfoActivity : AppCompatActivity() {
    private lateinit var viewModel: OrganisationInfoViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organisation_info)

        val organisationUrl = intent.getStringExtra("OrganisationUrl")
        val organisationId = intent.getStringExtra("OrganisationId")

        viewModel = ViewModelProviders.of(this).get(OrganisationInfoViewModel::class.java)

        viewModel.getInformation(organisationUrl!!, MyApplication.getEmail()!!)
        viewModel.info.observe(this, Observer {info ->
            title = info.name
            OrganisationDetail.text = info.details
            adapter.update(getEmployees(info.employees!!))

            if(!isOrganisationsAppointmentsAlreadyActivated(organisationUrl)) {
                btShareData.visibility = View.VISIBLE
            } else if(info.isClientRegistered!!) {
                btShareData.setText(R.string.btActivateOrganisation)
            }

            btShareData.setOnClickListener {
                if(!info.isClientRegistered!!) {
                    val intent = Intent(this, NewClientActivity::class.java)
                    intent.putExtra("PersonalInfos", info.clientPersonalInfoFields?.toTypedArray())
                    intent.putExtra("OrganisationUrl", organisationUrl)
                    intent.putExtra("OrganisationId", organisationId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Ön már korábban regisztrált a szervezethez, mostantól már a naptárban is láthatja időpontjait.", Toast.LENGTH_LONG).show()
                }
            }
        })
        initRecyclerView()
    }

    private fun isOrganisationsAppointmentsAlreadyActivated(organisationUrl: String): Boolean{
        val mapString = MyApplication.secureSharedPreferences.getString("OrganisationsMap", "")
        val organisationMap = mapString?.toHashMap()
        organisationMap?.let { map ->
            if(map.containsKey(organisationUrl)){
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
        recyclerView = OrganisationInfoRecyclerView
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