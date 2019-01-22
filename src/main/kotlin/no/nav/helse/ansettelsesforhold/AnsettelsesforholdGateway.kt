package no.nav.helse.ansettelsesforhold

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.prometheus.client.Histogram
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import java.net.URL

private val ansettelsesforholdOppslagHistogram = Histogram.build(
    "histogram_oppslag_ansettelsesforhold",
    "Tidsbruk for oppslag på arbeidsforhold for søker"
).register()

private val ansettelsesforholdOppslagCounter = monitoredOperationtCounter(
    name = "counter_oppslag_ansettelsesforhold",
    help = "Antall oppslag gjort på arbeidsforhold for person"
)

class AnsettelsesforholdGateway(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val tokenProvider: ServiceAccountTokenProvider
) {
    suspend fun getAnsettelsesforhold(fnr: Fodselsnummer) : List<Ansettelsesforhold> {
        val sparkelResponse = request(fnr)
        val ansettelsesforhold = mutableListOf<Ansettelsesforhold>()

        sparkelResponse.arbeidsforhold.forEach {arbeidsforhold ->
            if (arbeidsforhold.arbeidsgiver.isOrganization()) {
                ansettelsesforhold.add(
                    Ansettelsesforhold(
                        navn = arbeidsforhold.arbeidsgiver.navn!!,
                        organisasjonsnummer = arbeidsforhold.arbeidsgiver.orgnummer!!
                    )
                )
            }
        }

        return ansettelsesforhold.toList()
    }

    private suspend fun request(fnr: Fodselsnummer) : SparkelResponse {
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "arbeidsforhold",
                aktoerService.getAktorId(fnr).value
            )
        )

        val httpRequest = prepareHttpRequestBuilder(
            authorization = tokenProvider.getAuthorizationHeader(),
            url = url
        )

        return monitoredOperation(
            operation = { httpClient.get<SparkelResponse>(httpRequest) },
            histogram = ansettelsesforholdOppslagHistogram,
            counter = ansettelsesforholdOppslagCounter
        )
    }
}


data class SparkelArbeidsGiver(val orgnummer: String?, val navn: String?) {
    fun isOrganization() : Boolean {
        return orgnummer != null && navn != null
    }
}
data class SparkelArbeidsforhold(val arbeidsgiver: SparkelArbeidsGiver)
data class SparkelResponse(val arbeidsforhold: Set<SparkelArbeidsforhold>) // Kan å samme arbeidsgiver flere ganger, så bruker Set istedenfor List