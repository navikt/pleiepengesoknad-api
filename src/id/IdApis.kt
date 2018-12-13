package no.nav.pleiepenger.api.id

import io.ktor.application.call
import io.ktor.locations.*
import io.ktor.response.respond

import io.ktor.routing.Route
import no.nav.pleiepenger.api.general.validation.ValidationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.constraints.Pattern

fun Route.idApis(
    validationHandler: ValidationHandler,
    idGateway: IdGateway
) {

    val logger: Logger = LoggerFactory.getLogger("nav.defaultStatusPages")


    @Location("/id/{fnr}")
    class getId(
        @get:Pattern(regexp = "\\d{11}") val fnr: kotlin.String
    )

    get { it: getId ->
        validationHandler.validate(it)
        val id = idGateway.getId()
        call.respond(IdResponse(id = id))
    }
}
