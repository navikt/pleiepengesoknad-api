package no.nav.helse.person

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import no.nav.helse.aktoer.AktoerId
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration
import java.time.LocalDate


private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"
private val logger: Logger = LoggerFactory.getLogger("nav.PersonGateway")


class PersonGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    private val baseUrl: URL,
    private val systemCredentialsProvider: SystemCredentialsProvider
) {
    suspend fun hentPerson(
        aktoerId: AktoerId,
        callId : CallId
    ) : Person {
        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "person",
                aktoerId.value
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

        return Person(
            fornavn = response.fornavn,
            mellomnavn = response.mellomnavn,
            etternavn = response.etternavn,
            fodselsdato = response.fdato
        )
    }

    private suspend fun request(
        httpRequest: HttpRequestBuilder
    ) : SparkelResponse {
        return Retry.retry(
            operation = "hente-person",
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

data class SparkelResponse(val fornavn: String, val mellomnavn: String?, val etternavn: String, val fdato: LocalDate)