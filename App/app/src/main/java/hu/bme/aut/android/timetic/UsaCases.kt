package hu.bme.aut.android.timetic

import com.google.firebase.crashlytics.FirebaseCrashlytics
import hu.bme.aut.android.timetic.data.model.Appointment
import hu.bme.aut.android.timetic.data.model.Person
import hu.bme.aut.android.timetic.dataManager.DBRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UseCases {
    companion object {
        fun logBackendError(e: Throwable, code: Int?, call: String){
            when(code) {
                400 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "400 - Bad Request")
                401 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "401 - Unauthorized ")
                403 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "403 - Forbidden")
                404 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "404 - Not Found")
                409 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "409 - Conflict")
                500 -> FirebaseCrashlytics.getInstance().setCustomKey("Code", "505 - Internal Server Error")
            }
            FirebaseCrashlytics.getInstance().setCustomKey("Call", call)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }


    fun appointmentOrganizer(
        repo: DBRepository,
        scope: CoroutineScope,
        backendList: List<Appointment>,
        dbList: List<Appointment>
    ) {
        val appointmentIds = ArrayList<String>()

        for (item in backendList){
            appointmentIds.add(item.backendId+item.organizationUrl)
            if(newOrUpdatedAppointment(repo, scope, dbList, item)){
                insert(repo, scope, item)
            }
        }
        deleteCanceledAppointments(repo, scope, dbList, appointmentIds)
    }

    //checks if the appointment already saved
    private fun newOrUpdatedAppointment(
        repo: DBRepository,
        scope: CoroutineScope,
        dbList: List<Appointment>,
        appointment: Appointment
    ) : Boolean {
        for(item in dbList){
            if(item.backendId == appointment.backendId){
                if(item.note == appointment.note && item.start_date.timeInMillis == appointment.start_date.timeInMillis &&
                    item.end_date.timeInMillis == appointment.end_date.timeInMillis &&
                    item.private_appointment == appointment.private_appointment &&
                    item.location == appointment.location && appointment.private_appointment){
                    return false
                }
                if(item.note == appointment.note && item.start_date.timeInMillis == appointment.start_date.timeInMillis &&
                    item.end_date.timeInMillis == appointment.end_date.timeInMillis && item.price!! == appointment.price &&
                    item.private_appointment == appointment.private_appointment && item.videochat == appointment.videochat &&
                    item.location == appointment.location && item.personBackendId == appointment.personBackendId && item.activity == appointment.activity){
                    return false
                }
                delete(repo, scope, item)
                return true
            }
        }
        return true
    }

    //delete if some Appointment was deleted at the server side
    private fun deleteCanceledAppointments(
        repo: DBRepository,
        scope: CoroutineScope,
        dbList: List<Appointment>,
        ids: ArrayList<String>
    ) {
        for(item in dbList){
            if(!ids.contains(item.backendId + item.organizationUrl)){
                delete(repo, scope, item)
            }
        }
    }

    fun personOrganizer(
        repo: DBRepository,
        scope: CoroutineScope,
        backendList: List<Person>,
        dbList: List<Person>
    ) {
        val clientIds = ArrayList<String>()
        val clientAlreadyAdded = ArrayList<String>()

        for (item in backendList){
            clientIds.add(item.backendId+item.email)
            if(!clientAlreadyAdded.contains(item.backendId+item.email) && newOrUpdatedClient(repo, scope, dbList, item) ){
                clientAlreadyAdded.add(item.backendId+item.email)
                insert(repo, scope, item)
            }
        }
        deleteClientsWithoutAppointment(repo, scope, dbList, clientIds)
    }

    //delete if some Client does not have any appointment
    private fun deleteClientsWithoutAppointment(
        repo: DBRepository,
        scope: CoroutineScope,
        dbList: List<Person>,
        ids: ArrayList<String>
    ) {
        for(item in dbList){
            if(!ids.contains(item.backendId + item.email)){
                delete(repo, scope, item)
            }
        }
    }

    //checks if the client already saved
    private fun newOrUpdatedClient(
        repo: DBRepository,
        scope: CoroutineScope,
        dbList: List<Person>,
        person: Person
    ) : Boolean {
        for(item in dbList){
            if(item.backendId == person.backendId){
                if(item.name == person.name && item.email == person.email && item.phone == person.phone ){
                    return false
                }
                delete(repo, scope, item)
                return true
            }
        }
        return true
    }

    private fun insert(repo: DBRepository, scope: CoroutineScope, appointment: Appointment) = scope.launch {
        repo.insert(appointment)
    }

    private fun insert(repo: DBRepository, scope: CoroutineScope, person: Person) = scope.launch {
        repo.insert(person)
    }

    private fun delete(repo: DBRepository, scope: CoroutineScope, appointment: Appointment) = scope.launch {
        repo.deleteAppointment(appointment)
    }

    private fun delete(repo: DBRepository, scope: CoroutineScope, person: Person) = scope.launch {
        repo.deletePerson(person)
    }

}