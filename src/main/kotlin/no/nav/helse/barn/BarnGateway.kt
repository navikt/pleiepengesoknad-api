package no.nav.helse.barn

import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.get
import io.ktor.http.*
import no.nav.helse.barn.sparkel.SparkelGetBarnResponse
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.error.CommunicationException
import no.nav.helse.general.extractFodselsdato
import no.nav.helse.id.Id
import no.nav.helse.id.IdNotFoundException
import no.nav.helse.id.IdService

class BarnGateway(
    private val httpClient: HttpClient,
    private val baseUrl: Url,
    private val idService: IdService
) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<KomplettBarn> {

        val response: SparkelGetBarnResponse = try {
            request(idService.getId(fnr))
        } catch (cause: IdNotFoundException) {
            request(idService.refreshAndGetId(fnr))

        }
        return mapResponse(response)
    }

    private suspend fun request(id: Id) : SparkelGetBarnResponse {
        val url = URLBuilder()
            .takeFrom(baseUrl)
            .path(baseUrl.fullPath, "id", id.value, "barn")
            .build()
        try {
            return httpClient.get(url.toString())
        } catch (cause: BadResponseStatusException) {
            if (HttpStatusCode.NotFound == cause.statusCode) {
                throw IdNotFoundException()
            } else {
                throw CommunicationException(url, cause.response)
            }
        } catch (cause: Throwable) {
            throw CommunicationException(url, cause)
        }
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