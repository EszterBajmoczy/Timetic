package hu.bme.aut.android.timetic.views_viewmodels.organizationoperations

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
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.recyclerViewAdapter.OrganizationAdapter
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import hu.bme.aut.android.timetic.views_viewmodels.login.afterTextChanged
import hu.bme.aut.android.timetic.views_viewmodels.organizationinfo.OrganizationInfoActivity
import kotlinx.android.synthetic.main.organization_operation_fragment.*
import java.lang.Exception

class OrganizationOperationFragment : Fragment(), OrganizationAdapter.OrganizationClickListener {
    private lateinit var viewModel: OrganizationOperationViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrganizationAdapter

    private val internetStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                tNoInternetConnectionOrganizationOperations.visibility = View.GONE
                viewModel.fetchData()
                viewModel.organizations.observe(requireActivity(), Observer {
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
        return inflater.inflate(R.layout.organization_operation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OrganizationOperationViewModel::class.java)

        initRecyclerView()
        removeFloatingActionButton()

        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if(isNetworkAvailable) {
            viewModel.fetchData()
            viewModel.organizations.observe(requireActivity(), Observer {
                adapter.update(it)
            })
        } else {
            tNoInternetConnectionOrganizationOperations.visibility = View.VISIBLE

            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context?.registerReceiver(internetStateChangedReceiver, intentFilter)
        }
        SearchOrganization.afterTextChanged {
            adapter.filter.filter(it)
        }
    }

    private fun initRecyclerView() {
        recyclerView = OrganizationRecyclerView
        adapter = OrganizationAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun removeFloatingActionButton(){
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.GONE
    }

    override fun onItemClick(organization: CommonOrganization) {
        val intent = Intent(requireContext(), OrganizationInfoActivity::class.java)
        intent.putExtra("OrganizationUrl", organization.serverUrl)
        intent.putExtra("OrganizationId", organization.id)
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