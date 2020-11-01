package hu.bme.aut.android.timetic.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.timetic.MyApplication
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.network.models.CommonClient
import kotlinx.android.synthetic.main.activity_new_client.*
import kotlinx.android.synthetic.main.item_new_client.view.*


class NewClientActivity : AppCompatActivity() {
    private lateinit var viewModel: NewClientViewModel
    private var ids = ArrayList<Int>()
    private var infos = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_client)

        title = "Új ügyfél felvétele"

        val pref = MyApplication.secureSharedPreferences
        etClientEmail.setText(pref.getString("Email", ""))

        viewModel = ViewModelProviders.of(this).get(NewClientViewModel::class.java)
        viewModel.data.observe(this, Observer { list ->
            val infoList = list.clientPersonalInfoFields

            infoList?.let {
                val mInflater = LayoutInflater.from(applicationContext)
                val mDynamicLayoutsContainer = findViewById<LinearLayout>(R.id.tableNewClient)

                for((i, item) in it.withIndex()){
                    val firstI = View.generateViewId()
                    val secondI = View.generateViewId()

                    infos.add(item)
                    ids.add(secondI)

                    val itemRow = mInflater.inflate(R.layout.item_new_client, null, false)
                    //text field
                    itemRow.textNewClient.text = item
                    itemRow.textNewClient.id = firstI
                    //editText field
                    itemRow.editTextNewClient.id = secondI

                    mDynamicLayoutsContainer.addView(itemRow)
                }
            }
        })
        btSaveClient.setOnClickListener {
            if(check()){
                val c = getClient()
                viewModel.addClient(c)
                finish()
            }
            else{
                Toast.makeText(this, "Kérem töltse ki az összes mezőt", Toast.LENGTH_LONG).show()
            }

        }

        btCancelClient.setOnClickListener {
            finish()
        }
    }

    private fun check() : Boolean {
        for((index, item) in infos.withIndex()){
            val view = findViewById<EditText>(ids[index])
            if(view.text.toString() == ""){
                return false
            }
        }
        if(etClientName.text.toString() == "" || etClientEmail.text.toString() == "" || etClientPhone.text.toString() == ""){
            return false
        }
        return true
    }

    private fun getClient(): CommonClient{
        val personalInfos = HashMap<String, String>()
        for((index, item) in infos.withIndex()){
            val view = findViewById<EditText>(ids[index])
            personalInfos[item] = view.text.toString()
        }
        return CommonClient(
            name = etClientName.text.toString(),
            email = etClientEmail.text.toString(),
            phone = etClientPhone.text.toString(),
            personalInfos = personalInfos
        )
    }
}