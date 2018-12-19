package no.nav.helse.barn

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer


@KtorExperimentalLocationsAPI
fun Route.barnApis(
    barnService: BarnService
) {

    @Location("/barn")
    class getBarn

    get { _: getBarn ->
        call.respond(
            BarnResponse(
                barnService.getBarn(getFodselsnummer(call))
            )
        )
    }

}