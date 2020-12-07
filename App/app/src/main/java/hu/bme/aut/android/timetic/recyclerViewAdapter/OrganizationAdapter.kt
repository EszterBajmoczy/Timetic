package hu.bme.aut.android.timetic.recyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.network.models.CommonOrganization
import java.util.*
import kotlin.collections.ArrayList

class OrganizationAdapter(
    private val listener: OrganizationClickListener
) :
    RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder>(), Filterable {

    private var list = mutableListOf<CommonOrganization>()
    private var originalList = mutableListOf<CommonOrganization>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizationViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_organization_list, parent, false)
        return OrganizationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrganizationViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name

        holder.item = item
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OrganizationClickListener {
        fun onItemClick(organization: CommonOrganization)
    }

    inner class OrganizationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.organizationName)

        var item: CommonOrganization? = null

        init {
            itemView.setOnClickListener {
                item?.let { location -> listener.onItemClick(location) }
            }
        }
    }

    fun addItem(item: CommonOrganization) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    fun update(items: List<CommonOrganization>) {
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
                list = results.values as MutableList<CommonOrganization>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filteredResults: List<CommonOrganization?>?
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
    private fun getFilteredResults(constraint: String?): List<CommonOrganization>? {
        val results: MutableList<CommonOrganization> = ArrayList()
        for (item in originalList) {
            if (constraint?.let { item.name!!.toLowerCase(Locale.getDefault()).contains(it) }!!) {
                results.add(item)
            }
        }
        return results
    }
}