package no.nav.pleiepenger.api.soknad

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import no.nav.pleiepenger.api.general.validation.ValidationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.soknadApis(
    validationHandler: ValidationHandler
) {

    val logger: Logger = LoggerFactory.getLogger("soknadApis")

    @Location("/soker/{id}/soknad")
    class sendSoknad(
        val id: kotlin.String
    )

    post { it : sendSoknad ->
        val entity = call.receive<Soknad>()
        validationHandler.validate(entity)
        call.respondText { entity.toString() + it.id }
    }
}
