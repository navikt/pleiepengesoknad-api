package no.nav.helse.soker

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration

private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"
private val logger: Logger = LoggerFactory.getLogger("nav.SokerGateway")


class SokerGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemCredentialsProvider: SystemCredentialsProvider
) {
    suspend fun getSoker(fnr: Fodselsnummer,
                         callId : CallId) : Soker {
        val url = Url.buildURL(
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

        val response = request(httpRequest)

        return Soker(
            fornavn = response.fornavn,
            mellomnavn = response.mellomnavn,
            etternavn = response.etternavn,
            fodselsnummer = fnr.value
        )
    }

    private suspend fun request(
        httpRequest: HttpRequestBuilder
    ) : SparkelResponse {
        return Retry.retry(
            operation = "hente-soker",
            tries = 3,
            initialDelay = Duration.ofMillis(100),
            maxDelay = Duration.ofMillis(300),
            logger = logger
        ) {
            monitoredHttpClient.requestAndReceive<SparkelResponse>(
                httpRequestBuilder = HttpRequestBuilder().takeFrom(httpRequest)
            )
        }
    }
}

data class SparkelResponse(val fornavn: String, val mellomnavn: String?, val etternavn: String)