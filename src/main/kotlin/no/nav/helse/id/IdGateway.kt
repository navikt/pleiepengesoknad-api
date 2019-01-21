package no.nav.helse.id

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import no.nav.helse.general.ServiceAccountTokenProvider
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.buildURL
import no.nav.helse.general.prepareHttpRequestBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

class IdGateway(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
    private val tokenProvider: ServiceAccountTokenProvider
) {

    val logger: Logger = LoggerFactory.getLogger("nav.IdGateway")

    suspend fun getId(fnr: Fodselsnummer) : Id {
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("api", "ident"),
            queryParameters = mapOf(Pair("fnr", fnr.value))
        )
        val httpRequest = prepareHttpRequestBuilder(
            authorization = tokenProvider.getAuthorizationHeader(),
            url = url
        )
        val response = httpClient.get<IdResponse>(httpRequest)
        logger.info("id='{}'", response)
        return Id(response.id)
    }
}