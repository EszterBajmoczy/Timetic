package hu.bme.aut.android.timetic.ui.clientoperations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.adapter.ClientAdapter
import hu.bme.aut.android.timetic.create.NewClientActivity
import hu.bme.aut.android.timetic.ui.loginAregistration.login.afterTextChanged
import kotlinx.android.synthetic.main.fragment_client_operations.*
import java.lang.Exception


class ClientOperationsFragment : Fragment(){

    private lateinit var viewModel: ClientOperationsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientAdapter

    private val internetStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            if(isNetworkAvailable) {
                tNoInternetConnectionClientOperations.visibility = View.GONE

                viewModel.fetchData(false, MyApplication.getOrganizationUrl()!!,
                    MyApplication.getToken()!!)

                viewModel._persons.observe(viewLifecycleOwner, Observer {
                    adapter.update(it)
                })
                context.unregisterReceiver(this)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_client_operations, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ClientOperationsViewModel::class.java)

        initRecyclerView()
        setFloatingActionButton()

        if(!MyApplication.getOrganizationUrl()!!.isNullOrEmpty()) {
            initialize(false)
        } else {
            initialize(true)
        }

        SearchClient.afterTextChanged {
            adapter.filter.filter(it)
        }
    }

    private fun initialize(local: Boolean) {
        //check internet connection
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if(isNetworkAvailable){
            viewModel.fetchData(local, MyApplication.getOrganizationUrl()!!,
                MyApplication.getToken()!!)
        } else {
            tNoInternetConnectionClientOperations.visibility = View.VISIBLE

            viewModel.fetchData(true, MyApplication.getOrganizationUrl()!!,
                MyApplication.getToken()!!)

            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context?.registerReceiver(internetStateChangedReceiver, intentFilter)
        }

        viewModel.persons.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })
    }

    override fun onPause() {
        try {
            context?.unregisterReceiver(internetStateChangedReceiver)
        } catch (e: Exception) {

        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if(!MyApplication.getOrganizationUrl()!!.isNullOrEmpty()){
            initialize(true)
        }
    }

    private fun initRecyclerView() {
        recyclerView = ClientRecyclerView
        adapter = ClientAdapter(this::callCallBack, this::emailCallBack)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
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

    private fun setFloatingActionButton(){
        if(MyApplication.getOrganizationUrl().isNullOrEmpty() || MyApplication.getOrganizationUrl() == ""){
            val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
            fab.visibility = View.GONE
        } else {
            val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
            fab.setOnClickListener {
                val intent = Intent(activity, NewClientActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
