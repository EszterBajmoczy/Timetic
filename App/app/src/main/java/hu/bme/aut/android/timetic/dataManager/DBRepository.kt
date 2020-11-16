package hu.bme.aut.android.timetic.dataManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.database.CalendarTypeConverter
import hu.bme.aut.android.timetic.database.RoomDao
import hu.bme.aut.android.timetic.database.models.RoomAppointment
import hu.bme.aut.android.timetic.database.models.RoomPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DBRepository(private val roomDao: RoomDao) {
    private val calendarTypeConverter = CalendarTypeConverter()

    suspend fun deleteAllTables() = withContext(Dispatchers.IO) {
        roomDao.deletePersonTable()
        roomDao.deleteAppointmentTable()
    }

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
            person = person,
            personPhone = personPhone,
            personEmail = personEmail,
            activity = activity,
            organisationUrl = organisationUrl
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
            person = person,
            personPhone = personPhone,
            personEmail = personEmail,
            activity = activity,
            organisationUrl = organisationUrl
        )
    }

    //Person
    fun getAllPersons(): LiveData<List<Person>> {
        return roomDao.getAllPersons()
            .map {roomPersons ->
                roomPersons.map {roomPerson ->
                    roomPerson.toDomainModel() }
            }
    }

    fun getPersonList(): List<Person> {
        val result = ArrayList<Person>()
        val list = roomDao.getPersonList()
        for(item in list){
            result.add(item.toDomainModel())
        }
        return result
    }

    suspend fun insert(person: Person) = withContext(Dispatchers.IO) {
        roomDao.insertPerson(person.toRoomModel())
    }

    suspend fun updatePerson(person: Person)  = withContext(Dispatchers.IO){
        roomDao.updatePerson(person.toRoomModel())

    }

    suspend fun deletePerson(person: Person) = withContext(Dispatchers.IO) {
        roomDao.deletePerson(person.toRoomModel())
    }

    private fun RoomPerson.toDomainModel(): Person {
        return Person(
            id = id,
            netId = netId,
            name = name,
            email = email,
            phone = phone
        )
    }

    private fun Person.toRoomModel(): RoomPerson {
        return RoomPerson(
            id = id,
            netId = netId,
            name = name,
            email = email,
            phone = phone
        )
    }
}