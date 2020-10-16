package hu.bme.aut.android.timetic.data.model

//TODO clear some attributes
class Employee (
    val id: Long? = null,
    val name: String,
    val email: String,
    val phone: String,
    val can_add_client: Boolean,
    val activity_id: Int,
    val company_id: Int,
    val appointment_id: Int
)