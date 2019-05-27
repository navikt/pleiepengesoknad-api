package no.nav.helse.barn

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import no.nav.helse.aktoer.AktoerId
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration
import java.time.LocalDate

private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"
private val logger: Logger = LoggerFactory.getLogger("nav.BarnGateway")

class BarnGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemCredentialsProvider: SystemCredentialsProvider
) {
    suspend fun hentBarn(
        fnr: Fodselsnummer,
        callId : CallId
    ) : List<Barn> {
        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "person",
                aktoerService.getAktorId(fnr, callId).value,
                "barn"
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

        return response.barn.map { sparkelBarn ->
            Barn(
                aktoerId = AktoerId(sparkelBarn.aktørId),
                fornavn = sparkelBarn.fornavn,
                mellomnavn = sparkelBarn.mellomnavn,
                etternavn = sparkelBarn.etternavn,
                fodselsdato = sparkelBarn.fdato,
                status = sparkelBarn.status
            )
        }
    }

    private suspend fun request(
        httpRequest: HttpRequestBuilder
    ) : SparkelResponse {
        return Retry.retry(
            operation = "hente-barn",
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

data class SparkelResponse(val barn: List<SparkelBarn>)
data class SparkelBarn(val fornavn: String, val mellomnavn: String?, val etternavn: String, val fdato: LocalDate, val status: String, val aktørId: String)