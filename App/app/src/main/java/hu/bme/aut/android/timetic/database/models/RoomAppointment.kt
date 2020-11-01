package hu.bme.aut.android.timetic.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointment")
class RoomAppointment (
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val netId: String,
    val note: String?,
    val start_date: Long,
    val end_date: Long,
    val price: Double? = null,
    val private_appointment: Boolean,
    val videochat: Boolean?,
    val address: String?,
    val client: String?,
    val activity: String?
)
