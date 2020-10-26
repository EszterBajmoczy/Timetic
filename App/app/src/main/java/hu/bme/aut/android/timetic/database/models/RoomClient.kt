package hu.bme.aut.android.timetic.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client")
class RoomClient (
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val netId: String,
    val name: String,
    val email: String,
    val phone: String
)