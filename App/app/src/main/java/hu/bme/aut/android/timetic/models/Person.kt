package hu.bme.aut.android.timetic.models

class Person(
    val id: Long? = null,
    val backendId: String,
    val name: String,
    val email: String,
    val phone: String
): Comparable<Person> {
    override fun compareTo(other: Person): Int {
        return name.compareTo(other.name)
    }
}