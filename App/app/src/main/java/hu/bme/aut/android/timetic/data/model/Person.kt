package hu.bme.aut.android.timetic.data.model

class Person(
    val id: Long? = null,
    val netId: String,
    val name: String,
    val email: String,
    val phone: String
): Comparable<Person> {
    override fun compareTo(other: Person): Int {
        return name.compareTo(other.name)
    }
}