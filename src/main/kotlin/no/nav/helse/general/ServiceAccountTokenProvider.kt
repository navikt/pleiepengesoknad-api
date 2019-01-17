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
    url: URL,
    private val httpClient: HttpClient
) {

    private var cachedToken: String? = null
    private var expiry: LocalDateTime? = null

    private val httpRequestBuilder: HttpRequestBuilder = HttpRequestBuilder()
    private val completeUrl : URL = if (scopes.isEmpty()) url else URL("$url&scope=${getScopesAsSpaceDelimitedList(scopes)}")

    init {
        httpRequestBuilder.header("Authorization", getAuthorizationHeader(username, password))
        httpRequestBuilder.header("Accept", "application/json")
        httpRequestBuilder.url(completeUrl)
    }

    suspend fun getToken() : String {
        if (hasCachedToken() && isCachedTokenValid()) {
            return cachedToken!!
        }

        clearCachedData()

        try {
            val response = httpClient.get<Response>(httpRequestBuilder)
            setCachedData(response)
            return cachedToken!!
        } catch (cause: Throwable) {
            throw IllegalStateException("Unable to retrieve service account access token from '$completeUrl' with username '$username'")
        }
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
    return Base64.getEncoder().encodeToString(auth.toByteArray())
}

private fun getScopesAsSpaceDelimitedList(scopes : List<String>) : String {
    return scopes.joinToString(separator = " ")
}