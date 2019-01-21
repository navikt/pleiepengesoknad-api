package no.nav.helse.ansettelsesforhold

import io.ktor.client.HttpClient
import no.nav.helse.general.ServiceAccountTokenProvider
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.buildURL
import no.nav.helse.general.lookupThroughSparkel
import no.nav.helse.id.IdService
import java.net.URL

class AnsettelsesforholdGateway(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
    private val idService: IdService,
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
            pathParts = listOf("api","arbeidsforhold")
        )

        return lookupThroughSparkel(
            httpClient = httpClient,
            url = url,
            idService = idService,
            tokenProvider = tokenProvider,
            fnr = fnr
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