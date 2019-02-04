package no.nav.helse.ansettelsesforhold

import io.ktor.client.HttpClient
import io.prometheus.client.Histogram
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.general.*
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.systembruker.SystemBrukerTokenService
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ansettelsesforholdOppslagHistogram = Histogram.build(
    "histogram_oppslag_ansettelsesforhold",
    "Tidsbruk for oppslag på arbeidsforhold for søker"
).register()

class AnsettelsesforholdGateway(
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
    ) : List<Ansettelsesforhold> {
        val sparkelResponse = request(fnr, callId, fraOgMed, tilOgMed)
        val ansettelsesforhold = mutableListOf<Ansettelsesforhold>()

        sparkelResponse.organisasjoner.forEach {arbeidsforhold ->
            ansettelsesforhold.add(
                Ansettelsesforhold(
                    navn = arbeidsforhold.navn,
                    organisasjonsnummer = arbeidsforhold.organisasjonsnummer
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
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "arbeidsforhold",
                aktoerService.getAktorId(fnr, callId).value
            ),
            queryParameters = mapOf(
                Pair("fom", DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed)),
                Pair("tom", DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed))
            )
        )

        val httpRequest = prepareHttpRequestBuilder(
            authorization = systemBrukerTokenService.getAuthorizationHeader(),
            url = url,
            callId = callId,
            apiGatewayApiKey = apiGatewayApiKey
        )

        return monitoredHttpRequest(
            httpClient = httpClient,
            httpRequest = httpRequest,
            histogram = ansettelsesforholdOppslagHistogram
        )
    }
}

data class SparkelArbeidsforhold(val organisasjonsnummer: String, val navn: String?)
data class SparkelResponse(val organisasjoner: Set<SparkelArbeidsforhold>) // Kan å samme arbeidsgiver flere ganger, så bruker Set istedenfor List