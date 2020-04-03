package no.nav.helse.soker

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.sokerApis")


@KtorExperimentalLocationsAPI
fun Route.sokerApis(
    sokerService: SokerService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/soker")
    class getSoker

    get { _: getSoker ->
        call.respond(sokerService.getSoker(
            idToken = idTokenProvider.getIdToken(call),
            callId = call.getCallId()
        ))
    }

    @Location("/ettersending/soker")
    class ettersendingSøker

    get { _: ettersendingSøker ->
        call.respond(sokerService.getSoker(
            idToken = idTokenProvider.getIdToken(call),
            callId = call.getCallId()
        ))
    }

}
