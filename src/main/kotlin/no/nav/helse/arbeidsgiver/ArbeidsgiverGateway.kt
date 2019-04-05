package no.nav.helse.arbeidsgiver

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger: Logger = LoggerFactory.getLogger("nav.ArbeidsgiverGateway")
private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"

class ArbeidsgiverGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemCredentialsProvider: SystemCredentialsProvider
) {

    suspend fun getAnsettelsesforhold(
        fnr: Fodselsnummer,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Arbeidsgiver> {
        val sparkelResponse = try { request(fnr, callId, fraOgMed, tilOgMed) } catch (cause: Throwable) {
            logger.error("Feil ved oppslag på arbeidsgivere. Returnerer tom liste med arbeidsgivere.", cause)
            SparkelResponse(arbeidsgivere = setOf())
        }
        val ansettelsesforhold = mutableListOf<Arbeidsgiver>()

        sparkelResponse.arbeidsgivere.forEach {arbeidsforhold ->
            ansettelsesforhold.add(
                Arbeidsgiver(
                    navn = arbeidsforhold.navn,
                    organisasjonsnummer = arbeidsforhold.orgnummer
                )
            )
        }

        return ansettelsesforhold.toList()
    }

    private suspend fun request(
        fnr: Fodselsnummer,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : SparkelResponse {
        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "arbeidsgivere",
                aktoerService.getAktorId(fnr, callId).value
            ),
            queryParameters = mapOf(
                Pair("fom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))),
                Pair("tom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed)))
            )
        )

        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemCredentialsProvider.getAuthorizationHeader())
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value) // For proxy
        httpRequest.header(SPARKEL_CORRELATION_ID_HEADER, callId.value)
        httpRequest.accept(ContentType.Application.Json)
        httpRequest.method = HttpMethod.Get
        httpRequest.url(url)

        return monitoredHttpClient.requestAndReceive(
            httpRequestBuilder = httpRequest
        )
    }
}

data class SparkelArbeidsforhold(val orgnummer: String, val navn: String?)
data class SparkelResponse(val arbeidsgivere: Set<SparkelArbeidsforhold>)