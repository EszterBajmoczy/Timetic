package hu.bme.aut.android.timetic.dataManager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Client
import hu.bme.aut.android.timetic.database.CalendarTypeConverter
import hu.bme.aut.android.timetic.database.RoomDao
import hu.bme.aut.android.timetic.database.models.RoomAppointment
import hu.bme.aut.android.timetic.database.models.RoomClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DBRepository(private val roomDao: RoomDao) {
    private val calendarTypeConverter = CalendarTypeConverter()

    //Appointment
    fun getAllAppointments(): LiveData<List<Appointment>> {
        return roomDao.getAllAppointments()
            .map {roomAppointments ->
                roomAppointments.map {roomAppointment ->
                    roomAppointment.toDomainModel() }
            }
    }

    fun getAppointmentList(): List<Appointment> {
        val result = ArrayList<Appointment>()
        val list = roomDao.getAppointmentList()
        for(item in list){
            result.add(item.toDomainModel())
        }
        return result
    }

    fun getAppointmentByNetId(netId: String): LiveData<Appointment> {
        return roomDao.getAppointmentByNetId(netId)
            .map {roomAppointments ->
                roomAppointments.toDomainModel()
            }
    }

    suspend fun deleteAppointmentByNetId(netId: String) = withContext(Dispatchers.IO) {
        roomDao.deleteAppointmentByNetId(netId)
    }

    suspend fun insert(appointment: Appointment) = withContext(Dispatchers.IO) {
        roomDao.insertAppointment(appointment.toRoomModel())
    }

    suspend fun updateAppointment(appointment: Appointment)  = withContext(Dispatchers.IO){
        roomDao.updateAppointment(appointment.toRoomModel())

    }

    suspend fun deleteAppointment(appointment: Appointment) = withContext(Dispatchers.IO) {
        roomDao.deleteAppointment(appointment.toRoomModel())
        Log.d("EZAZ", "delete success")
    }

    private fun RoomAppointment.toDomainModel(): Appointment {
        return Appointment(
            id = id,
            netId = netId,
            note = note,
            start_date = calendarTypeConverter.toCalendar(start_date),
            end_date = calendarTypeConverter.toCalendar(end_date),
            price = price,
            private_appointment = private_appointment,
            videochat = videochat,
            address = address,
            client = client,
            activity = activity
        )
    }

    private fun Appointment.toRoomModel(): RoomAppointment {
        return RoomAppointment(
            id = id,
            netId = netId,
            note = note,
            start_date = calendarTypeConverter.toLong(start_date),
            end_date = calendarTypeConverter.toLong(end_date),
            price = price,
            private_appointment = private_appointment,
            videochat = videochat,
            address = address,
            client = client,
            activity = activity
        )
    }

    //Client
    fun getAllClients(): LiveData<List<Client>> {
        return roomDao.getAllClients()
            .map {roomClients ->
                roomClients.map {roomClient ->
                    roomClient.toDomainModel() }
            }
    }

    fun getClientList(): List<Client> {
        val result = ArrayList<Client>()
        val list = roomDao.getClientList()
        for(item in list){
            result.add(item.toDomainModel())
        }
        return result
    }

    suspend fun insert(client: Client) = withContext(Dispatchers.IO) {
        roomDao.insertClient(client.toRoomModel())
    }

    suspend fun updateClient(client: Client)  = withContext(Dispatchers.IO){
        roomDao.updateClient(client.toRoomModel())

    }

    suspend fun deleteClient(client: Client) = withContext(Dispatchers.IO) {
        roomDao.deleteClient(client.toRoomModel())
    }

    private fun RoomClient.toDomainModel(): Client {
        return Client(
            id = id,
            netId = netId,
            name = name,
            email = email,
            phone = phone
        )
    }

    private fun Client.toRoomModel(): RoomClient {
        return RoomClient(
            id = id,
            netId = netId,
            name = name,
            email = email,
            phone = phone
        )
    }
}