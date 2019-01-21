package no.nav.helse.general

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import java.net.URL
import java.time.LocalDateTime
import java.util.*

class ServiceAccountTokenProvider(
    private val username: String,
    password: String,
    scopes: List<String>,
    baseUrl: URL,
    private val httpClient: HttpClient
) { // TODO: Implement Readiness

    private var cachedToken: String? = null
    private var expiry: LocalDateTime? = null

    private val httpRequestBuilder: HttpRequestBuilder
    private val completeUrl : URL

    init {
        val queryParameters : MutableMap<String, String> = mutableMapOf(Pair("grant_type","client_credentials"))
        if (!scopes.isEmpty()) {
            queryParameters["scope"] = getScopesAsSpaceDelimitedList(scopes)
        }

        completeUrl = buildURL(baseUrl = baseUrl, queryParameters = queryParameters)

        httpRequestBuilder = prepareHttpRequestBuilder(
            authorization = getAuthorizationHeader(username, password),
            url = completeUrl
        )
    }

    private suspend fun getToken() : String {
        if (hasCachedToken() && isCachedTokenValid()) {
            return cachedToken!!
        }

        clearCachedData()

        try {
            val response = httpClient.get<Response>(httpRequestBuilder)
            setCachedData(response)
            return cachedToken!!
        } catch (cause: Throwable) {
            throw IllegalStateException("Unable to retrieve service account access token from '$completeUrl' with username '$username'", cause)
        }
    }

    suspend fun getAuthorizationHeader() : String {
        return "Bearer ${getToken()}"
    }

    private fun setCachedData(response: Response) {
        cachedToken = response.accessToken
        expiry = LocalDateTime.now()
            .plusSeconds(response.expiresIn)
            .minusSeconds(10L)
    }

    private fun clearCachedData() {
        cachedToken = null
        expiry = null
    }

    private fun hasCachedToken() : Boolean {
        return cachedToken != null && expiry != null
    }

    private fun isCachedTokenValid() : Boolean {
        return expiry!!.isAfter(LocalDateTime.now())
    }
}

private data class Response(val accessToken : String, val expiresIn: Long)

private fun getAuthorizationHeader(username : String, password: String) : String {
    val auth = "$username:$password"
    return "Basic ${Base64.getEncoder().encodeToString(auth.toByteArray())}"
}

private fun getScopesAsSpaceDelimitedList(scopes : List<String>) : String {
    return scopes.joinToString(separator = " ")
}