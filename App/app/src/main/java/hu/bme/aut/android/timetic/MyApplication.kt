package hu.bme.aut.android.timetic

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import hu.bme.aut.android.timetic.database.Database

class MyApplication : Application() {

	companion object {
		lateinit var myDatabase: Database
	  		private set
		var developerBaseUrl = "https://optipus.ddns.net:8080"

		lateinit var secureSharedPreferences: SharedPreferences
		lateinit var appContext: Context
		fun getToken(): String? {
			return secureSharedPreferences.getString("Token", "")
		}

		fun getDevToken(): String? {
			return secureSharedPreferences.getString("DevToken", "")
		}

		fun getRefreshToken(): String? {
			return secureSharedPreferences.getString("RefreshToken", "")
		}

		fun getOrganizationUrl(): String? {
			return secureSharedPreferences.getString("OrganizationUrl", "")
		}

		fun getEmail(): String? {
			return secureSharedPreferences.getString("Email", "")
		}
	}
	
	override fun onCreate() {
		super.onCreate()

		myDatabase = Room.databaseBuilder(
                    applicationContext,
                    Database::class.java,
                    "timetic_database"
                ).build()

		val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
		val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

		secureSharedPreferences = EncryptedSharedPreferences.create(
			"encrypted_shared_preferences",
			masterKeyAlias,
			applicationContext,
			EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
			EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
		)

		appContext = applicationContext
	}




}
