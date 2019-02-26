package no.nav.helse.soknad

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.prometheus.client.Histogram
import no.nav.helse.general.CallId
import no.nav.helse.general.HttpRequest
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.systembruker.SystemBrukerTokenService
import java.net.URL

private val leggTilProsesseringHistogram = Histogram.build(
    "histogram_legge_soknad_til_prosessering",
    "Tidsbruk for å legge søknad til prosessering"
).register()

class PleiepengesoknadProsesseringGateway(
    private val httpClient: HttpClient,
    baseUrl : URL,
    private val systemBrukerTokenService: SystemBrukerTokenService,
    private val apiGatewayApiKey: ApiGatewayApiKey
){

    private val komplettUrl = HttpRequest.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "soknad")
    )

    suspend fun leggTilProsessering(
        soknad : KomplettSoknad,
        callId: CallId
    ) {
        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemBrukerTokenService.getAuthorizationHeader())
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value)
        httpRequest.header(HttpHeaders.ContentType, ContentType.Application.Json)
        httpRequest.header(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
        httpRequest.method = HttpMethod.Post
        httpRequest.body = soknad
        httpRequest.url(komplettUrl)

        HttpRequest.monitored<HttpResponse>(
            httpClient = httpClient,
            httpRequest = httpRequest,
            expectedStatusCodes = listOf(HttpStatusCode.Accepted),
            histogram = leggTilProsesseringHistogram
        )
    }
}