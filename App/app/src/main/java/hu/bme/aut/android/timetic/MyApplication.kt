package hu.bme.aut.android.timetic

import android.app.Application
import androidx.room.Room
import hu.bme.aut.android.timetic.database.Database

class MyApplication : Application() {

	companion object {
		lateinit var myDatabase: Database
	  		private set
	}
	
	override fun onCreate() {
		super.onCreate()

		myDatabase = Room.databaseBuilder(
                    applicationContext,
                    Database::class.java,
                    "my_database"
                ).build()
	}
	
}