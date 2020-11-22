package hu.bme.aut.android.timetic.ui.loginAregistration.logout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.R
import kotlinx.android.synthetic.main.fragment_logout.*


class LogoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = false
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        logoutYes.setOnClickListener {
            context?.sendBroadcast(Intent("Logout"))
        }

        logoutNo.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.isVisible = true
        super.onDestroyView()
    }
}