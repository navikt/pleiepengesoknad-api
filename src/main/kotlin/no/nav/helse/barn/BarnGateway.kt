package no.nav.helse.barn

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.barn.sparkel.SparkelGetBarnResponse
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import java.net.URL

class BarnGateway(
    private val httpClient : HttpClient,
    private val baseUrl : URL,
    private val aktoerService: AktoerService,
    private val tokenProvider : ServiceAccountTokenProvider
) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<KomplettBarn> {
        return mapResponse(request(fnr))
    }

    private suspend fun request(fnr: Fodselsnummer) : SparkelGetBarnResponse {
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "barn",
                aktoerService.getAktorId(fnr).value
            )
        )

        val httpRequest = prepareHttpRequestBuilder(
            authorization = tokenProvider.getAuthorizationHeader(),
            url = url
        )

        return httpClient.get(httpRequest)
    }

    private fun mapResponse(sparkelResponse : SparkelGetBarnResponse) : List<KomplettBarn> {
        val barn = mutableListOf<KomplettBarn>()
        sparkelResponse.barn.forEach {
            val fnr = Fodselsnummer(it.fodselsnummer)
            barn.add(
                KomplettBarn(
                    fornavn = it.fornavn,
                    etternavn = it.etternavn,
                    mellomnavn = it.mellomnavn,
                    fodselsnummer = fnr,
                    fodselsdato = extractFodselsdato(fnr)
                )
            )
        }
        return barn.toList()
    }
}