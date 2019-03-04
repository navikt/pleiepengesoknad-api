package no.nav.helse.arbeidsgiver

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"

private val ansettelsesforholdOppslagHistogram = Histogram.build(
    "histogram_oppslag_ansettelsesforhold",
    "Tidsbruk for oppslag på arbeidsforhold for søker"
).register()

class ArbeidsgiverGateway(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemBrukerTokenService: SystemBrukerTokenService,
    private val apiGatewayApiKey: ApiGatewayApiKey
) {
    suspend fun getAnsettelsesforhold(
        fnr: Fodselsnummer,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Arbeidsgiver> {
        val sparkelResponse = request(fnr, callId, fraOgMed, tilOgMed)
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
        val url = HttpRequest.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "arbeidsgivere",
                aktoerService.getAktorId(fnr, callId).value
            ),
            queryParameters = mapOf(
                Pair("fom", DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed)),
                Pair("tom", DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed))
            )
        )

        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemBrukerTokenService.getAuthorizationHeader(callId))
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value) // For proxy
        httpRequest.header(SPARKEL_CORRELATION_ID_HEADER, callId.value)
        httpRequest.accept(ContentType.Application.Json)
        httpRequest.header(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
        httpRequest.method = HttpMethod.Get
        httpRequest.url(url)

        return HttpRequest.monitored(
            httpClient = httpClient,
            httpRequest = httpRequest,
            histogram = ansettelsesforholdOppslagHistogram
        )
    }
}

data class SparkelArbeidsforhold(val orgnummer: String, val navn: String?)
data class SparkelResponse(val arbeidsgivere: Set<SparkelArbeidsforhold>)