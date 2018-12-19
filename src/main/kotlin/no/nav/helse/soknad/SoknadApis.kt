package no.nav.helse.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.routing.Route
import no.nav.helse.general.validation.ValidationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")


@KtorExperimentalLocationsAPI
fun Route.soknadApis(
    validationHandler: ValidationHandler,
    soknadKafkaProducer: SoknadKafkaProducer
) {

    @Location("/soknad")
    class sendSoknad

    post { _ : sendSoknad ->
        val entity = call.receive<Soknad>()
        validationHandler.validate(entity)
        soknadKafkaProducer.produce(soknad = entity)
        call.response.status(HttpStatusCode.Accepted)
    }
}