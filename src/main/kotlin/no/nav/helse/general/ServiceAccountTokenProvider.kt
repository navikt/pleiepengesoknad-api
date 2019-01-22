package no.nav.helse.general

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.prometheus.client.Histogram
import no.nav.helse.monitorering.Readiness
import no.nav.helse.monitorering.ReadinessResult
import java.net.URL
import java.time.LocalDateTime
import java.util.*

private val getAccessTokenHistogram = Histogram.build(
    "histogram_hente_service_account_acesss_token",
    "Tidsbruk for henting av Service Account Access Tokens"
).register()

private val getAccessTokenCounter = monitoredOperationtCounter(
    name = "counter_hente_service_account_acesss_token",
    help = "Antall Service Account Access Tokens hentet"
)

class ServiceAccountTokenProvider(
    private val username: String,
    password: String,
    scopes: List<String>,
    baseUrl: URL,
    private val httpClient: HttpClient
) : Readiness {

    override suspend fun getResult(): ReadinessResult {
        try {
            getToken()
            return ReadinessResult(isOk = true, message = "Successfully retrieved Service Account Access Token")
        } catch (cause: Throwable) {
            return ReadinessResult(isOk = false, message = "Error retrieving Service Account Token : '$cause.message'")

        }
    }

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
            val response = monitoredOperation(
                operation = { httpClient.get<Response>(httpRequestBuilder) },
                counter = getAccessTokenCounter,
                histogram = getAccessTokenHistogram
            )
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