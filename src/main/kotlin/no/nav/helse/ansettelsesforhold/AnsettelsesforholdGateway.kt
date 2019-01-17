package no.nav.helse.ansettelsesforhold

import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import no.nav.helse.general.ServiceAccountTokenProvider
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.error.CommunicationException
import no.nav.helse.id.Id
import no.nav.helse.id.IdNotFoundException
import no.nav.helse.id.IdService

class AnsettelsesforholdGateway(
    private val httpClient: HttpClient,
    private val baseUrl: Url,
    private val idService: IdService,
    private val tokenProvider: ServiceAccountTokenProvider
) {
    suspend fun getAnsettelsesforhold(fnr: Fodselsnummer) : List<Ansettelsesforhold> {
        return try {
            request(idService.getId(fnr)).ansettelsesforhold
        } catch (cause: IdNotFoundException) {
            request(idService.refreshAndGetId(fnr)).ansettelsesforhold
        }
    }

    private suspend fun request(id: Id) : AnsettelsesforholdResponse { // TODO: Ikke bruk samme klasse som API'et returnerer
        val url = URLBuilder()
            .takeFrom(baseUrl)
            .path(baseUrl.fullPath, "id", id.value, "ansettelsesforhold")
            .build()
        try {
            val requestBuilder = HttpRequestBuilder()
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("Authorization", tokenProvider.getAuthorizationHeader())
            requestBuilder.url(url.toString())

            return httpClient.get(requestBuilder)
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
}