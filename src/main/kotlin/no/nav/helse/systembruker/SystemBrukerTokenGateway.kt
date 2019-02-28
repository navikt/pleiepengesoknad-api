package no.nav.helse.systembruker

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
    clientId: String,
    clientSecret: String,
    scopes: List<String>,
    baseUrl: URL,
    apiGatewayApiKey: ApiGatewayApiKey,
    private val httpClient: HttpClient
) {
    private val httpRequest: HttpRequestBuilder
    private val completeUrl : URL

    init {
        val queryParameters : MutableMap<String, String> = mutableMapOf(Pair("grant_type","client_credentials"))
        if (!scopes.isEmpty()) {
            queryParameters["scope"] = getScopesAsSpaceDelimitedList(scopes)
        }

        completeUrl = HttpRequest.buildURL(baseUrl = baseUrl, queryParameters = queryParameters)

        httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, getAuthorizationHeader(clientId, clientSecret))
        httpRequest.header(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
        httpRequest.method = HttpMethod.Get
        httpRequest.url(completeUrl)
    }

    internal suspend fun getToken() : Response {
        return HttpRequest.monitored(
            httpClient = httpClient,
            httpRequest = HttpRequestBuilder().takeFrom(httpRequest),
            histogram = getAccessTokenHistogram
        )
    }
}

data class Response(val accessToken : String, val expiresIn: Long)

private fun getAuthorizationHeader(clientId : String, clientSecret: String) : String {
    val auth = "$clientId:$clientSecret"
    return "Basic ${Base64.getEncoder().encodeToString(auth.toByteArray())}"
}

private fun getScopesAsSpaceDelimitedList(scopes : List<String>) : String {
    return scopes.joinToString(separator = " ")
}