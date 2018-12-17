package no.nav.pleiepenger.api.barn

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.pleiepenger.api.general.auth.getFodselsnummer


fun Route.barnApis(
    barnService: BarnService
) {

    @Location("/barn")
    class getBarn

    get { it: getBarn ->
        call.respond(
            BarnResponse(
                barnService.getBarn(getFodselsnummer(call))
            )
        )
    }

}