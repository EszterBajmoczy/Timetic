package hu.bme.aut.android.timetic

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.database.Database
import hu.bme.aut.android.timetic.network.apiOrganisation.OrganisationApi
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

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

		fun getOrganisationUrl(): String? {
			return secureSharedPreferences.getString("OrganisationUrl", "")
		}

		fun getEmail(): String? {
			return secureSharedPreferences.getString("Email", "")
		}

		fun getOrganisationApiForRefresh(): OrganisationApi {
			val m = Moshi.Builder()
				.add(KotlinJsonAdapterFactory())
				.build()

			var client: OkHttpClient? = null
			client =  OkHttpClient.Builder()
				.addInterceptor(HttpBearerAuth(
					"bearer",
					getRefreshToken()!!
				))
				.build()

			val retrofit = Retrofit.Builder()
				.baseUrl(secureSharedPreferences.getString("OrganisationUrl", "").toString())
				.client(client)
				.addConverterFactory(MoshiConverterFactory.create(m))
				.build()

			return retrofit.create(OrganisationApi::class.java)
		}
	}
	
	override fun onCreate() {
		super.onCreate()

		myDatabase = Room.databaseBuilder(
                    applicationContext,
                    Database::class.java,
                    "my_database"
                ).build()

		val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
		val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

		secureSharedPreferences = EncryptedSharedPreferences.create(
			"secure_shared_preferences",
			masterKeyAlias,
			applicationContext,
			EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
			EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
		)

		appContext = applicationContext
	}




}
