package no.nav.helse.soker

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.helse.SØKER_URL
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId

fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    get(SØKER_URL) {
        call.respond(søkerService.getSoker(
            idToken = idTokenProvider.getIdToken(call),
            callId = call.getCallId()
        ))
    }
}
