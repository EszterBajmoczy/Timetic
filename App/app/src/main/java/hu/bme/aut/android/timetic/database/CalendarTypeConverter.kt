package hu.bme.aut.android.timetic.database

import androidx.room.TypeConverter
import java.util.*

class CalendarTypeConverter {
    @TypeConverter
    fun toCalendar(millis: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar
    }

    @TypeConverter
    fun toLong(calendar: Calendar): Long {
        return calendar.timeInMillis
    }
}