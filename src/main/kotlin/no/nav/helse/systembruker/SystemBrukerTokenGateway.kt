package no.nav.helse.systembruker

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.prometheus.client.Histogram
import no.nav.helse.general.*
import no.nav.helse.general.auth.ApiGatewayApiKey
import java.net.URL
import java.util.*

private val getAccessTokenHistogram = Histogram.build(
    "histogram_hente_system_bruker_acesss_token",
    "Tidsbruk for henting av system bruker Access Tokens"
).register()

class SystemBrukerTokenGateway(
    private val username: String,
    password: String,
    scopes: List<String>,
    baseUrl: URL,
    apiGatewayApiKey: ApiGatewayApiKey,
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
            url = completeUrl,
            apiGatewayApiKey = apiGatewayApiKey
        )
    }

    internal suspend fun getToken() : Response {
        return monitoredHttpRequest(
            httpClient = httpClient,
            httpRequest = HttpRequestBuilder().takeFrom(httpRequestBuilder),
            histogram = getAccessTokenHistogram
        )
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