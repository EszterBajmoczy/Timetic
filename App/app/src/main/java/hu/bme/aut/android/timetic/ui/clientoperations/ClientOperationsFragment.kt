package hu.bme.aut.android.timetic.ui.clientoperations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.R

class ClientOperationsFragment : Fragment() {

    private lateinit var clientOperationsViewModel: ClientOperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        clientOperationsViewModel =
            ViewModelProviders.of(this).get(ClientOperationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_client_operations, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        clientOperationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
