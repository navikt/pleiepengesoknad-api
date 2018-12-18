package no.nav.pleiepenger.api.id

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.takeFrom
import no.nav.pleiepenger.api.general.auth.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdGateway(
    private val httpClient: HttpClient,
    private val baseUri: Url
) {

    val logger: Logger = LoggerFactory.getLogger("nav.IdGateway")

    suspend fun getId(fnr: Fodselsnummer) : Id {
        val url = URLBuilder().takeFrom(baseUri).path(baseUri.fullPath, "fnr", fnr.value).buildString()
        logger.info("Requesting url '{}'", url)
        val response = httpClient.get<IdResponse>(url)
        logger.info("id='{}'", response)
        return Id(response.id)
    }
}