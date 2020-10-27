package hu.bme.aut.android.timetic.dataManager

import android.util.Log
import hu.bme.aut.android.timetic.network.apiOrganisation.EmployeeApi
import hu.bme.aut.android.timetic.network.models.CommonToken
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


class AccessTokenInterceptor  : Authenticator {

    lateinit var api: NetworkOrganisationInteractor

    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjA0NkM0MTM2LUZGQkEtNEQzRi1BRjQ0LTIyMkYwQUY0OUUyNSIsImV4cCI6MTYwMzgzMTExOC43NTMzMDY5LCJzdWIiOiJhZG1pbit0b2tlbiIsInB1ciI6ImFjY2VzcyJ9.zz7D4XgODZfpFweyDx8vkZZ_Df8lTZ9npRguT7Im_1I"
        if (!isRequestWithAccessToken(response) || accessToken == null) {
            return null
        }
        synchronized(this) {
            val newAccessToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjA0NkM0MTM2LUZGQkEtNEQzRi1BRjQ0LTIyMkYwQUY0OUUyNSIsImV4cCI6MTYwMzgzMTExOC43NTMzMDY5LCJzdWIiOiJhZG1pbit0b2tlbiIsInB1ciI6ImFjY2VzcyJ9.zz7D4XgODZfpFweyDx8vkZZ_Df8lTZ9npRguT7Im_1I"
            // Access token is refreshed in another thread.
            if (accessToken != newAccessToken) {
                return newRequestWithAccessToken(response.request, newAccessToken)
            }

            // Need to refresh an access token
            val updatedAccessToken: String = ""
            api.getToken(onSuccess = this::success, onError = this::error)
            return newRequestWithAccessToken(response.request, updatedAccessToken)
        }
    }

    private fun isRequestWithAccessToken(response: Response): Boolean {
        val header = response.request.header("Authorization")
        return header != null && header.startsWith("Bearer")
    }

    private fun newRequestWithAccessToken(
        request: Request,
        accessToken: String
    ): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }

    private fun error(e: Throwable) {
        Log.d("EZAZ", "interceptor errrrrror")
        //TODO
    }

    private fun success(tokon: CommonToken)  {
        Log.d("EZAZ", "interceptor success")

    }
}