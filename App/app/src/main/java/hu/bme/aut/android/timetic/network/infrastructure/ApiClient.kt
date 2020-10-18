package hu.bme.aut.android.timetic.network.infrastructure

import com.squareup.moshi.Moshi
import hu.bme.aut.android.timetic.network.auth.HttpBasicAuth
import hu.bme.aut.android.timetic.network.auth.HttpBearerAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class ApiClient(
    private var baseUrl: String = defaultBasePath,
    private val okHttpClientBuilder: OkHttpClient.Builder? = null,
    private val serializerBuilder: Moshi.Builder = Serializer.moshiBuilder,
    private val okHttpClient : OkHttpClient? = null
) {
    private val apiAuthorizations = mutableMapOf<String, Interceptor>()
    var logger: ((String) -> Unit)? = null

    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(serializerBuilder.build()))
    }

    private val clientBuilder: OkHttpClient.Builder by lazy {
        okHttpClientBuilder ?: defaultClientBuilder
    }

    private val defaultClientBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    logger?.invoke(message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
    }

    init {
        normalizeBaseUrl()
    }

    constructor(
        baseUrl: String = defaultBasePath,
        okHttpClientBuilder: OkHttpClient.Builder? = null,
        serializerBuilder: Moshi.Builder = Serializer.moshiBuilder,
        authNames: Array<String>
    ) : this(baseUrl, okHttpClientBuilder, serializerBuilder) {
        authNames.forEach { authName ->
            val auth = when (authName) {
                "basicAuth" -> HttpBasicAuth()
                "refreshTokenAuth" -> HttpBearerAuth("bearer")
                "tokenAuth" -> HttpBearerAuth("bearer")
                else -> throw RuntimeException("auth name $authName not found in available auth names")
            }
            addAuthorization(authName, auth);
        }
    }

    constructor(
        baseUrl: String = defaultBasePath,
        okHttpClientBuilder: OkHttpClient.Builder? = null,
        serializerBuilder: Moshi.Builder = Serializer.moshiBuilder,
        authName: String, 
        username: String, 
        password: String
    ) : this(baseUrl, okHttpClientBuilder, serializerBuilder, arrayOf(authName)) {
        setCredentials(username, password)
    }

    constructor(
        baseUrl: String = defaultBasePath,
        okHttpClientBuilder: OkHttpClient.Builder? = null,
        serializerBuilder: Moshi.Builder = Serializer.moshiBuilder,
        authName: String,
        bearerToken: String
    ) : this(baseUrl, okHttpClientBuilder, serializerBuilder, arrayOf(authName)) {
        setBearerToken(bearerToken)
    }


    fun setCredentials(username: String, password: String): ApiClient {
        apiAuthorizations.values.runOnFirst<Interceptor, HttpBasicAuth> {
            setCredentials(username, password);
        }
        return this
    }

    fun setBearerToken(bearerToken: String): ApiClient {
        apiAuthorizations.values.runOnFirst<Interceptor, HttpBearerAuth> {
            this.bearerToken = bearerToken
        }
        return this
    }

    /**
     * Adds an authorization to be used by the client
     * @param authName Authentication name
     * @param authorization Authorization interceptor
     * @return ApiClient
     */
    fun addAuthorization(authName: String, authorization: Interceptor): ApiClient {
        if (apiAuthorizations.containsKey(authName)) {
            throw RuntimeException("auth name $authName already in api authorizations")
        }
        apiAuthorizations[authName] = authorization
        clientBuilder.addInterceptor(authorization)
        return this
    }

    fun setLogger(logger: (String) -> Unit): ApiClient {
        this.logger = logger
        return this
    }

    fun <S> createService(serviceClass: Class<S>): S {
        var usedClient: OkHttpClient? = null
        this.okHttpClient?.let { usedClient = it } ?: run {usedClient = clientBuilder.build()}
        return retrofitBuilder.client(usedClient).build().create(serviceClass)
    }

    private fun normalizeBaseUrl() {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
    }

    private inline fun <T, reified U> Iterable<T>.runOnFirst(callback: U.() -> Unit) {
        for (element in this) {
            if (element is U)  {
                callback.invoke(element)
                break
            }
        }
    }

    companion object {        
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty("hu.bme.aut.android.timetic.baseUrl", "https://virtserver.swaggerhub.com/mihalyk97/Szervezeti-backend-timetic/1.0.0")
        }
    }
}