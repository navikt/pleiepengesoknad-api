package no.nav.helse.barn

import io.ktor.client.HttpClient
import no.nav.helse.barn.sparkel.SparkelGetBarnResponse
import no.nav.helse.general.ServiceAccountTokenProvider
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.buildURL
import no.nav.helse.general.extractFodselsdato
import no.nav.helse.general.lookupThroughSparkel
import no.nav.helse.id.IdService
import java.net.URL

class BarnGateway(
    private val httpClient : HttpClient,
    private val baseUrl : URL,
    private val idService : IdService,
    private val tokenProvider : ServiceAccountTokenProvider
) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<KomplettBarn> {
        return mapResponse(request(fnr))
    }

    private suspend fun request(fnr: Fodselsnummer) : SparkelGetBarnResponse {
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("barn")
        )
        return lookupThroughSparkel(
            httpClient = httpClient,
            url = url,
            idService = idService,
            tokenProvider = tokenProvider,
            fnr = fnr
        )
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