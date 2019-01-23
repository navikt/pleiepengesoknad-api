package no.nav.helse.monitorering

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.response.respond
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import java.net.URL
import java.util.*

@KtorExperimentalLocationsAPI
fun Route.monitoreringApis(
    collectorRegistry: CollectorRegistry,
    readiness: List<Readiness>,
    pingUrls: List<URL>,
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
                val response = httpClient.call(pu.toString()).response
                if (HttpStatusCode.OK != response.status) {
                    errors.add("Tilkobling mot '$pu' feiler (med HTTP ${response.status})")
                } else {
                    success.add("Tilkobling mot '$pu' fungerer")
                }
            } catch (cause: Throwable) {
                errors.add("Tilkobling mot '$pu' feiler (med feilmeldingen '${cause.message}')")
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