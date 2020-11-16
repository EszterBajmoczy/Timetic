package hu.bme.aut.android.timetic.database

import androidx.lifecycle.LiveData
import androidx.room.*
import hu.bme.aut.android.timetic.database.models.RoomAppointment
import hu.bme.aut.android.timetic.database.models.RoomPerson

@Dao
interface RoomDao {

    //Appointment
    @Insert
    fun insertAppointment(appointment: RoomAppointment)

    @Query("SELECT * FROM appointment")
    fun getAllAppointments(): LiveData<List<RoomAppointment>>

    @Query("SELECT * FROM appointment")
    fun getAppointmentList(): List<RoomAppointment>

    @Query("SELECT * FROM appointment WHERE netId == :netId")
    fun getAppointmentByNetId(netId: String): LiveData<RoomAppointment>

    @Query("DELETE FROM appointment WHERE netId == :netId")
    fun deleteAppointmentByNetId(netId: String)

    @Update
    fun updateAppointment(appointment: RoomAppointment): Int

    @Delete
    fun deleteAppointment(appointment: RoomAppointment)

    @Query("DELETE FROM appointment")
    fun deleteAppointmentTable()

    //Client
    @Insert
    fun insertPerson(person: RoomPerson)

    @Query("SELECT * FROM person")
    fun getAllPersons(): LiveData<List<RoomPerson>>

    @Query("SELECT * FROM person")
    fun getPersonList(): List<RoomPerson>

    @Update
    fun updatePerson(person: RoomPerson): Int

    @Delete
    fun deletePerson(person: RoomPerson)

    @Query("DELETE FROM person")
    fun deletePersonTable()
}