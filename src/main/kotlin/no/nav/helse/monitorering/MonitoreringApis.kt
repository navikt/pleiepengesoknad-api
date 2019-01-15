package no.nav.helse.monitorering

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import java.util.*

@KtorExperimentalLocationsAPI
fun Route.monitoreringApis(
    collectorRegistry: CollectorRegistry
) {


    get("/isalive") {
        call.respondText("ALIVE", ContentType.Text.Plain)
    }

    get("/isready") {
        // TODO: Requeste Sparkel /isready og se at vi har kontakt med Kafka før vi sier READY?
        call.respondText("READY", ContentType.Text.Plain)
    }

    get("/metrics") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: Collections.emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}