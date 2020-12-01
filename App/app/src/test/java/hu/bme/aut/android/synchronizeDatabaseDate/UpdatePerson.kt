package hu.bme.aut.android.synchronizeDatabaseDate

import hu.bme.aut.android.timetic.UseCases
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

//Tests if the updated appointment from the server is also updated in the database
class UpdatePerson {
    private lateinit var mockRepository: DBRepository
    private var backendList = ArrayList<Person>()
    private var dbList = ArrayList<Person>()

    @Before
    fun setup() {
        mockRepository = mockkClass(DBRepository::class)

        var person1 = Person(
            id = null,
            backendId = "134124jkfs234df798eu",
            name = "Kiss Anna",
            email = "kiss.a@gmail.com",
            phone = "+36307657676"
        )
        val person2 = Person(
            id = null,
            backendId = "13674545gfsdg234df798eu",
            name = "Nagy Dóra",
            email = "nagy.d@gmail.com",
            phone = "+36303427676"
        )

        dbList.add(person1)
        dbList.add(person2)

        //it's updated
        person1 = Person(
            id = null,
            backendId = "134124jkfs234df798eu",
            name = "Kiss Anna",
            email = "kiss.anna@gmail.com",
            phone = "+36307657676"
        )

        backendList.add(person1)
        backendList.add(person2)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun updatePersons() = runBlockingTest{
        val result = ArrayList<Person>(dbList)
        val sumList = ArrayList<Person>()
        sumList.addAll(dbList)
        sumList.addAll(backendList)

        //check if the appointments from the backend was added, not deleted
        for(item in sumList) {
            coEvery {
                mockRepository.insert(item)
            } answers { result.add(item) }

            coEvery {
                mockRepository.deletePerson(item)
            } answers { result.remove(item) }
        }

        UseCases().personOrganizer(repo = mockRepository,
            scope = this, backendList = backendList, dbList = dbList)

        //check if they are the same
        Assert.assertTrue(result.containsAll(backendList))
        Assert.assertTrue(backendList.containsAll(result))
    }
}