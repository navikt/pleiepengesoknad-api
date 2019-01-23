package no.nav.helse.systembruker

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.prometheus.client.Histogram
import no.nav.helse.general.buildURL
import no.nav.helse.general.monitoredOperation
import no.nav.helse.general.monitoredOperationtCounter
import no.nav.helse.general.prepareHttpRequestBuilder
import java.net.URL
import java.util.*

private val getAccessTokenHistogram = Histogram.build(
    "histogram_hente_syste_bruker_acesss_token",
    "Tidsbruk for henting av system bruker Access Tokens"
).register()

private val getAccessTokenCounter = monitoredOperationtCounter(
    name = "counter_hente_syste_bruker_acesss_token",
    help = "Antall system bruker Access Tokens hentet"
)

class SystemBrukerTokenGateway(
    private val username: String,
    password: String,
    scopes: List<String>,
    baseUrl: URL,
    private val httpClient: HttpClient
) {
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

    internal suspend fun getToken() : Response {
        try {
            return monitoredOperation(
                operation = { httpClient.get<Response>(httpRequestBuilder) },
                counter = getAccessTokenCounter,
                histogram = getAccessTokenHistogram
            )
        } catch (cause: Throwable) {
            throw IllegalStateException("Fikk ikke hentet system bruker access token fra'$completeUrl' med brukernavn '$username'", cause)
        }
    }
}

data class Response(val accessToken : String, val expiresIn: Long)

private fun getAuthorizationHeader(username : String, password: String) : String {
    val auth = "$username:$password"
    return "Basic ${Base64.getEncoder().encodeToString(auth.toByteArray())}"
}

private fun getScopesAsSpaceDelimitedList(scopes : List<String>) : String {
    return scopes.joinToString(separator = " ")
}