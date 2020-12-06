package hu.bme.aut.android.synchronizeDatabaseDate

import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.dataManager.DBRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

//Tests if the appointments are synchronized correctly after appointment cancellation
class CancelAppointment {
    private lateinit var mockRepository: DBRepository
    private var backendList = ArrayList<Appointment>()
    private var dbList = ArrayList<Appointment>()

    @Before
    fun setup() {
        mockRepository = mockkClass(DBRepository::class)

        val appointment1 = Appointment(
            id = null,
            backendId = "134124jkfsdf798eu",
            note = null,
            start_date = Calendar.getInstance(),
            end_date = getCalendarHoursLater(1),
            price = 8000.0,
            private_appointment = false,
            videochat = true,
            location = "Ügyfélnél",
            personBackendId = "234g2z47823zeuhfdsf",
            activity = "Arckezelés",
            organizationUrl = null
        )
        val appointment2 = Appointment(
            id = null,
            backendId = "134124jkfsdfsdf798eu",
            note = null,
            start_date = getCalendarHoursLater(3),
            end_date = getCalendarHoursLater(4),
            price = null,
            private_appointment = true,
            videochat = false,
            location = null,
            personBackendId = null,
            activity = null,
            organizationUrl = "https://optipus.ddns.net:8080"
        )

        backendList.add(appointment1)

        dbList.add(appointment1)
        dbList.add(appointment2)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun cancelAppointments() = runBlockingTest{
        val result = ArrayList<Appointment>(dbList)
        val sumList = ArrayList<Appointment>()
        sumList.addAll(dbList)
        sumList.addAll(backendList)

        //check what happend with the appointments from the database
        for(item in sumList) {
            coEvery {
                mockRepository.insert(item)
            } answers { result.add(item) }

            coEvery {
                mockRepository.deleteAppointment(item)
            } answers { result.remove(item) }
        }

        UseCases().appointmentOrganizer(repo = mockRepository,
            scope = this, backendList = backendList, dbList = dbList)

        //check if they are the same
        Assert.assertTrue(result.containsAll(backendList))
        Assert.assertTrue(backendList.containsAll(result))
    }

    private fun getCalendarHoursLater(hours: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        return calendar
    }
}