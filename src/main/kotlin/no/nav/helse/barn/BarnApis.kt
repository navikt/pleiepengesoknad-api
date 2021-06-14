package no.nav.helse.barn

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.helse.BARN_URL
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    get(BARN_URL) {
        call.respond(
            BarnResponse(
                barnService.hentNaaverendeBarn(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        )
    }
}
