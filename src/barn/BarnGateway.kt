package no.nav.pleiepenger.api.barn

import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.get
import io.ktor.http.*
import no.nav.pleiepenger.api.barn.sparkel.SparkelGetBarnResponse
import no.nav.pleiepenger.api.general.auth.Fodselsnummer
import no.nav.pleiepenger.api.general.error.CommunicationException
import no.nav.pleiepenger.api.id.Id
import no.nav.pleiepenger.api.id.IdNotFoundException
import no.nav.pleiepenger.api.id.IdService

class BarnGateway(
    private val httpClient: HttpClient,
    private val baseUrl: Url,
    private val idService: IdService
) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<Barn> {

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

    private fun mapResponse(sparkelResponse : SparkelGetBarnResponse) : List<Barn> {
        val barn = mutableListOf<Barn>()
        sparkelResponse.barn.forEach {
            barn.add(
                Barn(fornavn = it.fornavn,
                    etternavn = it.etternavn,
                    mellomnavn = it.mellomnavn,
                    fodselsdato = it.fodselsdato
                )
            )
        }
        return barn.toList()
    }
}