package hu.bme.aut.android.timetic.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.data.model.Client
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction1


class ClientAdapter(
    private val listener: ClientClickListener,
    private val callCallBack: KFunction1<@ParameterName(name = "number") String, Unit>,
    private val emailCallBack: KFunction1<@ParameterName(name = "email") String, Unit>
) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>(), Filterable {

    private var list = mutableListOf<Client>()
    private var originalList = mutableListOf<Client>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_client_list, parent, false)
        return ClientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name

        holder.item = item
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface ClientClickListener {
        fun onItemChanged(item: Client)
    }

    inner class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.clientName)
        val call: ImageView = itemView.findViewById(R.id.clientItemCall)
        val email: ImageView = itemView.findViewById(R.id.clientItemEmail)

        var item: Client? = null

        init {
            call.setOnClickListener {
                callCallBack(item!!.phone)
            }
            email.setOnClickListener {
                emailCallBack(item!!.email)
            }

        }
    }

    fun addItem(item: Client) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    fun update(items: List<Client>) {
        if(originalList.size == 0){
            originalList.addAll(items)
        }
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                list = results.values as MutableList<Client>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                var filteredResults: List<Client?>? = null
                if (constraint.length == 0) {
                    filteredResults = originalList
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase())
                }
                val results = FilterResults()
                results.values = filteredResults
                return results
            }
        }
    }
    private fun getFilteredResults(constraint: String?): List<Client>? {
        val results: MutableList<Client> = ArrayList()
        for (item in originalList) {
            if (constraint?.let { item.name.toLowerCase(Locale.getDefault()).contains(it) }!!) {
                results.add(item)
            }
        }
        return results
    }
}