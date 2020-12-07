package hu.bme.aut.android.timetic.recyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.data.model.Person
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction1

//Adapter for the client recycler view in the ClientOperationsFragment
class ClientAdapter(
    private val callCallBack: KFunction1<@ParameterName(name = "number") String, Unit>,
    private val emailCallBack: KFunction1<@ParameterName(name = "email") String, Unit>
) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>(), Filterable {

    private var list = mutableListOf<Person>()
    private var originalList = mutableListOf<Person>()

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

    inner class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.clientName)
        val call: ImageView = itemView.findViewById(R.id.clientItemCall)
        val email: ImageView = itemView.findViewById(R.id.clientItemEmail)

        var item: Person? = null

        init {
            call.setOnClickListener {
                callCallBack(item!!.phone)
            }
            email.setOnClickListener {
                emailCallBack(item!!.email)
            }

        }
    }

    fun addItem(item: Person) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    fun update(items: List<Person>) {
        originalList.clear()
        originalList.addAll(items)
        Collections.sort(items)
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
                list = results.values as MutableList<Person>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filteredResults: List<Person?>?
                if (constraint.isEmpty()) {
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
    private fun getFilteredResults(constraint: String?): List<Person>? {
        val results: MutableList<Person> = ArrayList()
        for (item in originalList) {
            if (constraint?.let { item.name.toLowerCase(Locale.getDefault()).contains(it) }!!) {
                results.add(item)
            }
        }
        return results
    }
}