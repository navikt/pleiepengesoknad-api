package no.nav.pleiepenger.api.id

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.takeFrom
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdGateway(
    private val httpClient: HttpClient,
    private val baseUri: Url
) {

    val logger: Logger = LoggerFactory.getLogger("IdGateway")

    suspend fun getId() : String {
        val url = URLBuilder().takeFrom(baseUri).path(baseUri.fullPath, "foo").buildString()
        logger.info("Requesting url '{}'", url)
        val response = httpClient.get<IdResponse>(url)
        logger.info("id='{}'", response)
        return response.id
    }
}