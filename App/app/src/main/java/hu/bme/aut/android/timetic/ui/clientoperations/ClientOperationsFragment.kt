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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.adapter.ClientAdapter
import hu.bme.aut.android.timetic.data.model.Client
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

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "secure_shared_preferences",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        //TODO internetconnection? -> ui, local: true/false
        viewModel.fetchData(true, secureSharedPreferences.getString("OrganisationUrl", "").toString(),
            secureSharedPreferences.getString("Token", "").toString())
        viewModel.clients.observe(viewLifecycleOwner, Observer {
            adapter.update(it)

            nameList = getNameList(it)
            val adapter: ArrayAdapter<String> =
                ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, nameList)

            SearchClient.threshold = 1

            SearchClient.setAdapter(adapter)


        })
        SearchClient.setOnItemClickListener { parent, view, position, id ->
            adapter.filter.filter(nameList[position])
        }
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
}
