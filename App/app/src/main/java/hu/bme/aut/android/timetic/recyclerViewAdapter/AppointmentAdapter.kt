package hu.bme.aut.android.timetic.recyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.timetic.R
import hu.bme.aut.android.timetic.models.Appointment
import java.util.*

class AppointmentAdapter(private val listener: AppointmentItemClickListener) :
    RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    private val items = mutableListOf<Appointment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_appointment_list, parent, false)
        return AppointmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val item = items[position]

        val time = "${format(item.start_date.get(Calendar.HOUR_OF_DAY))}:${format(item.start_date.get(Calendar.MINUTE))} " +
                "- ${format(item.end_date.get(Calendar.HOUR_OF_DAY))}:${format(item.end_date.get(Calendar.MINUTE))}"

        holder.name.text = item.activity
        holder.time.text = time

        holder.item = item
    }

    private fun format(value: Int) : String{
        if(value.toString().length == 1){
            return "${0}${value}"
        }
        return value.toString()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface AppointmentItemClickListener {
        fun onItemClick(appointment: Appointment)
    }

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.appointmentName)
        val time: TextView = itemView.findViewById(R.id.appointmentTime)

        var item: Appointment? = null

        init {
            itemView.setOnClickListener {
                item?.let { location -> listener.onItemClick(location) }
            }
        }
    }

    fun update(list: List<Appointment>) {
        items.clear()
        Collections.sort(list)
        items.addAll(list)
        notifyDataSetChanged()
    }
}