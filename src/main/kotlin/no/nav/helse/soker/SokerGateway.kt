package no.nav.helse.soker

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.prometheus.client.Histogram
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.general.*
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.systembruker.SystemBrukerTokenService
import java.net.URL

private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"

private val sokerOppslagHistogram = Histogram.build(
    "histogram_oppslag_soker",
    "Tidsbruk for oppslag på persondata om søker"
).register()

class SokerGateway(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemBrukerTokenService: SystemBrukerTokenService,
    private val apiGatewayApiKey: ApiGatewayApiKey
) {
    suspend fun getSoker(fnr: Fodselsnummer,
                         callId : CallId) : Soker {
        val url = HttpRequest.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "person",
                aktoerService.getAktorId(fnr, callId).value
            )
        )

        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemBrukerTokenService.getAuthorizationHeader(callId))
        httpRequest.header(SPARKEL_CORRELATION_ID_HEADER, callId.value)
        httpRequest.accept(ContentType.Application.Json)
        httpRequest.header(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
        httpRequest.method = HttpMethod.Get
        httpRequest.url(url)

        val response = HttpRequest.monitored<SparkelResponse> (
            httpClient = httpClient,
            httpRequest = httpRequest,
            histogram = sokerOppslagHistogram
        )

        return Soker(
            fornavn = response.fornavn,
            mellomnavn = response.mellomnavn,
            etternavn = response.etternavn,
            fodselsnummer = fnr.value
        )
    }
}

data class SparkelResponse(val fornavn: String, val mellomnavn: String?, val etternavn: String)