package hu.bme.aut.android.timetic.ui.clientoperations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.ui.loginAregistration.login.afterTextChanged
import kotlinx.android.synthetic.main.fragment_client_operations.*


class ClientOperationsFragment : Fragment(), ClientAdapter.ClientClickListener {

    private lateinit var viewModel: ClientOperationsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientAdapter
    private lateinit var nameList: List<String>

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

        //TODO check if filter works
        initRecyclerView()
        setFloatingActionButton()

        //TODO internetconnection? -> ui, local: true/false
        viewModel.fetchData(false, MyApplication.getOrganisationUrl()!!,
            MyApplication.getToken()!!)
        viewModel.clients.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })
        SearchClient.afterTextChanged {
            adapter.filter.filter(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData(false, MyApplication.getOrganisationUrl()!!,
            MyApplication.getToken()!!)
    }

    private fun getNameList(list: List<Client>): List<String> {
        val stringList = ArrayList<String>()
        for(item in list){
            stringList.add(item.name)
        }
        return stringList
    }

    private fun initRecyclerView() {
        recyclerView = ClientRecyclerView
        adapter = ClientAdapter(this, this::callCallBack, this::emailCallBack)
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

    override fun onItemChanged(item: Client) {
        //TODO?
        //Do nothing
    }

    fun setFloatingActionButton(){
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            //TODO
            val intent = Intent(activity, NewClientActivity::class.java)
            startActivity(intent)
        }
    }
}
