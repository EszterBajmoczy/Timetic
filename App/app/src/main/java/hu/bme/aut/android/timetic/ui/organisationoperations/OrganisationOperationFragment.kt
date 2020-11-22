package hu.bme.aut.android.timetic.ui.organisationoperations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.adapter.OrganisationAdapter
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.ui.loginAregistration.login.afterTextChanged
import hu.bme.aut.android.timetic.ui.organisationoperations.info.OrganisationInfoActivity
import kotlinx.android.synthetic.main.fragment_client_operations.*
import kotlinx.android.synthetic.main.organisation_operation_fragment.*
import java.lang.Exception

class OrganisationOperationFragment : Fragment(), OrganisationAdapter.OrganisationClickListener {
    private lateinit var viewModel: OrganisationOperationViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrganisationAdapter

    companion object {
        fun newInstance() = OrganisationOperationFragment()
    }

    private val internetStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                tNoInternetConnectionOrganisationOperations.visibility = View.GONE
                viewModel.fetchData()
                viewModel.organisations.observe(requireActivity(), Observer {
                    adapter.update(it)
                })
                context.unregisterReceiver(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.organisation_operation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OrganisationOperationViewModel::class.java)

        initRecyclerView()
        removeFloatingActionButton()

        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if(isNetworkAvailable) {
            viewModel.fetchData()
            viewModel.organisations.observe(requireActivity(), Observer {
                adapter.update(it)
            })
        } else {
            tNoInternetConnectionOrganisationOperations.visibility = View.VISIBLE

            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context?.registerReceiver(internetStateChangedReceiver, intentFilter)
        }
        SearchOrganisation.afterTextChanged {
            adapter.filter.filter(it)
        }
    }

    private fun initRecyclerView() {
        recyclerView = OrganisationRecyclerView
        adapter = OrganisationAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun removeFloatingActionButton(){
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.GONE
    }

    override fun onItemClick(organization: CommonOrganization) {
        val intent = Intent(requireContext(), OrganisationInfoActivity::class.java)
        intent.putExtra("OrganisationUrl", organization.serverUrl)
        intent.putExtra("OrganisationId", organization.id)
        startActivity(intent)
    }

    override fun onPause() {
        try {
            context?.unregisterReceiver(internetStateChangedReceiver)
        } catch (e: Exception) {

        }
        super.onPause()
    }
}