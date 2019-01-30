package no.nav.helse.barn

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.barn.sparkel.SparkelGetBarnResponse
import no.nav.helse.general.*
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.systembruker.SystemBrukerTokenService
import java.net.URL

class BarnGateway(
    private val httpClient : HttpClient,
    private val baseUrl : URL,
    private val aktoerService: AktoerService,
    private val systemBrukerTokenService: SystemBrukerTokenService,
    private val apiGatewayApiKey: ApiGatewayApiKey
) {
    suspend fun getBarn(
        fnr: Fodselsnummer,
        callId: CallId
    ) : List<KomplettBarn> {
        return mapResponse(request(fnr, callId))
    }

    private suspend fun request(
        fnr: Fodselsnummer,
        callId: CallId
    ) : SparkelGetBarnResponse {
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "barn",
                aktoerService.getAktorId(fnr, callId).value
            )
        )

        val httpRequest = prepareHttpRequestBuilder(
            authorization = systemBrukerTokenService.getAuthorizationHeader(),
            url = url,
            callId = callId,
            apiGatewayApiKey = apiGatewayApiKey
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