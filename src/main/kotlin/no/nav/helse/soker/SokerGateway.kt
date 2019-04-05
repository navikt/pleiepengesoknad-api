package no.nav.helse.soker

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import java.net.URL

private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"

class SokerGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemCredentialsProvider: SystemCredentialsProvider
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
        httpRequest.header(HttpHeaders.Authorization, systemCredentialsProvider.getAuthorizationHeader())
        httpRequest.header(SPARKEL_CORRELATION_ID_HEADER, callId.value)
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value) // For proxy
        httpRequest.accept(ContentType.Application.Json)
        httpRequest.method = HttpMethod.Get
        httpRequest.url(url)

        val response = monitoredHttpClient.requestAndReceive<SparkelResponse>(
            httpRequestBuilder = httpRequest
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