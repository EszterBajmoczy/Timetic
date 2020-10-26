package hu.bme.aut.android.timetic.ui.loginAregistration.logout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import hu.bme.aut.android.timetic.MainActivity
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.StartScreenActivity
import kotlinx.android.synthetic.main.fragment_logout.*


class LogoutFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        logoutYes.setOnClickListener {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

            val secureSharedPreferences = EncryptedSharedPreferences.create(
                "secure_shared_preferences",
                masterKeyAlias,
                requireActivity().applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val edit = secureSharedPreferences.edit()
            edit.clear()
            edit.apply()

            val intent = Intent(activity, StartScreenActivity::class.java)
            startActivity(intent)
        }

        logoutNo.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }


}