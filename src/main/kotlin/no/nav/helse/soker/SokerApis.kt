package no.nav.helse.soker

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.getNorskIdent
import no.nav.helse.general.getCallId
import no.nav.helse.k9.K9OppslagService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.sokerApis")


@KtorExperimentalLocationsAPI
fun Route.sokerApis(
    k9OppslagService: K9OppslagService
) {

    @Location("/soker")
    class getSoker

    get { _: getSoker ->
        call.respond(k9OppslagService.getSoker(
            norskIdent = call.getNorskIdent(),
            callId = call.getCallId()
        ))
    }
}
