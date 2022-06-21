package no.nav.helse.barn

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.BARN_URL
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import no.nav.helse.general.oppslag.TilgangNektetException
import no.nav.helse.soker.respondTilgangNektetProblemDetail

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    get(BARN_URL) {
        try {
            call.respond(
                BarnResponse(
                    barnService.hentNaaverendeBarn(
                        idToken = idTokenProvider.getIdToken(call),
                        callId = call.getCallId()
                    )
                )
            )
        } catch (e: Exception) {
            when(e) {
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(e)
                else -> throw e
            }
        }
    }
}
