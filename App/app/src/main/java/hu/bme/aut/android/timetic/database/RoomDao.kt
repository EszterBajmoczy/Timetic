package hu.bme.aut.android.timetic.database

import androidx.lifecycle.LiveData
import androidx.room.*
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.database.models.RoomAppointment
import hu.bme.aut.android.timetic.database.models.RoomClient

@Dao
interface RoomDao {

    //Appointment
    @Insert
    fun insertAppointment(appointment: RoomAppointment)

    @Query("SELECT * FROM appointment")
    fun getAllAppointments(): LiveData<List<RoomAppointment>>

    @Update
    fun updateAppointment(appointment: RoomAppointment): Int

    @Delete
    fun deleteAppointment(appointment: RoomAppointment)

    //Client
    @Insert
    fun insertClient(client: RoomClient)

    @Query("SELECT * FROM client")
    fun getAllClients(): LiveData<List<RoomClient>>

    @Update
    fun updateClient(client: RoomClient): Int

    @Delete
    fun deleteClient(client: RoomClient)
}