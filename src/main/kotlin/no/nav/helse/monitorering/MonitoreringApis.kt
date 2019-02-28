package no.nav.helse.monitorering

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.response.respond
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.helse.general.HttpRequest
import no.nav.helse.general.auth.ApiGatewayApiKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.monitoreringApis")


@KtorExperimentalLocationsAPI
fun Route.monitoreringApis(
    collectorRegistry: CollectorRegistry,
    readiness: List<Readiness>,
    pingUrls: List<URL>,
    apiGatewayPingUrls: List<URL>,
    apiGatewayApiKey: ApiGatewayApiKey,
    httpClient: HttpClient
) {


    get("/isalive") {
        call.respond(Response(status = "ALIVE", success = listOf("I am alive"), errors = emptyList()))
    }

    get("/isready") {
        call.respond(Response(status = "READY", success = listOf("I am ready"), errors = emptyList()))
    }

    get("/isready-deep") {
        val success = mutableListOf<String>()
        val errors = mutableListOf<String>()

        readiness.forEach { r ->
            if (r.getResult().isOk) {
                success.add(r.getResult().message)
            } else {
                errors.add(r.getResult().message)
            }
        }

        pingUrls.forEach { pu ->
            try {
                val httpRequest = httpRequest(
                    url = pu
                )
                HttpRequest.monitored<Any>(
                    httpClient = httpClient,
                    httpRequest = httpRequest
                )
                success.add("Tilkobling mot '$pu' fungerer")
            } catch (cause: Throwable) {
                logger.error("Readiness error", cause)
                errors.add("${cause.message}")
            }
        }

        apiGatewayPingUrls.forEach { pu ->
            try {
                val httpRequest = httpRequest(
                    url = pu,
                    apiGatewayApiKey = apiGatewayApiKey
                )
                HttpRequest.monitored<Any>(
                    httpClient = httpClient,
                    httpRequest = httpRequest
                )
                success.add("Tilkobling mot '$pu' fungerer")
            } catch (cause: Throwable) {
                logger.error("Readiness error", cause)
                errors.add("${cause.message}")
            }
        }


        val httpStatusCode = if(errors.isEmpty()) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
        val status = if(errors.isEmpty()) "READY" else "NOT_READY"

        call.respond(
            message = Response(
                errors = errors,
                success = success,
                status = status
            ),
            status = httpStatusCode
        )
    }

    get("/metrics") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: Collections.emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}

private data class Response (val status: String, val success: List<String>, val errors: List<String>)

private fun httpRequest(
    url: URL,
    apiGatewayApiKey: ApiGatewayApiKey? = null
) : HttpRequestBuilder {
    val httpRequest = HttpRequestBuilder()
    httpRequest.method = HttpMethod.Get
    if (apiGatewayApiKey != null) {
        httpRequest.header(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
    }
    httpRequest.url(url)
    return httpRequest
}