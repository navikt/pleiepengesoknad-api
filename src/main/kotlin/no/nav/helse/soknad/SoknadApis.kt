package no.nav.pleiepenger.api.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.routing.Route
import no.nav.helse.general.validation.ValidationHandler
import no.nav.helse.soknad.Soknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@KtorExperimentalLocationsAPI
fun Route.soknadApis(
    validationHandler: ValidationHandler
) {

    @Location("/soknad")
    class sendSoknad

    post { _ : sendSoknad ->
        val entity = call.receive<Soknad>()
        validationHandler.validate(entity)
        call.response.status(HttpStatusCode.Accepted)
    }
}