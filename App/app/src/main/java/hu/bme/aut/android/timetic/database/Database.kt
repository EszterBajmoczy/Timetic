package hu.bme.aut.android.timetic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bme.aut.android.timetic.database.models.RoomAppointment
import hu.bme.aut.android.timetic.database.models.RoomClient

@Database(
    version = 1,
    exportSchema = false,
    entities = [RoomClient::class, RoomAppointment::class]
)
@TypeConverters(
    CalendarTypeConverter::class
)

abstract class Database : RoomDatabase(){
    abstract fun roomDao(): RoomDao
}