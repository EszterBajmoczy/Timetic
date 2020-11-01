package hu.bme.aut.android.timetic

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.aut.android.timetic.dataManager.AuthorizationInterceptor
import hu.bme.aut.android.timetic.database.Database
import hu.bme.aut.android.timetic.network.apiOrganisation.EmployeeApi
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MyApplication : Application() {

	companion object {
		lateinit var myDatabase: Database
	  		private set
		var developerBaseUrl = "http://optipus.ddns.net:8080"

		lateinit var secureSharedPreferences: SharedPreferences
		lateinit var refreshToken: String
		lateinit var appContext: Context
		fun getToken(): String? {
			return secureSharedPreferences.getString("Token", "")
		}

		fun getOrganisationUrl(): String? {
			return secureSharedPreferences.getString("OrganisationUrl", "")
		}

		fun getOrganisationApiForRefresh(): EmployeeApi {
			val m = Moshi.Builder()
				.add(KotlinJsonAdapterFactory())
				.build()

			var client: OkHttpClient? = null
			client =  OkHttpClient.Builder()
				.addInterceptor(HttpBearerAuth(
					"bearer",
					refreshToken
				))
				.build()

			val retrofit = Retrofit.Builder()
				.baseUrl(secureSharedPreferences.getString("OrganisationUrl", "").toString())
				.client(client)
				.addConverterFactory(MoshiConverterFactory.create(m))
				.build()

			return retrofit.create(EmployeeApi::class.java)
		}

		fun getOrganisationApi(): EmployeeApi {
			return getApiService(secureSharedPreferences.getString("OrganisationUrl", "").toString(), null, HttpBearerAuth(
				"bearer",
				getToken()!!
			))
		}

		private fun getApiService(organisationUrl: String, auth: HttpBasicAuth?, autb: HttpBearerAuth?): EmployeeApi {
			val m = Moshi.Builder()
				.add(KotlinJsonAdapterFactory())
				.build()

			var client: OkHttpClient? = null
			if (auth != null) {
				client =  OkHttpClient.Builder()
					.addInterceptor(auth)
					.build()
			}
			else if(autb != null){
				client =  OkHttpClient.Builder()
					.addInterceptor(autb)
					.build()
			}
			else{
				client =  OkHttpClient.Builder()
					.addInterceptor(AuthorizationInterceptor())
					.build()
			}

			val retrofit = Retrofit.Builder()
				.baseUrl(organisationUrl)
				.client(client)
				.addConverterFactory(MoshiConverterFactory.create(m))
				.build()

			return retrofit.create(EmployeeApi::class.java)
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

		refreshToken = secureSharedPreferences.getString("RefreshToken", "")!!

		appContext = applicationContext
	}




}
