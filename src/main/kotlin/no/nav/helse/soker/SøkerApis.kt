package no.nav.helse.soker

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.SØKER_URL
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import no.nav.helse.general.getCallId
import no.nav.helse.general.oppslag.TilgangNektetException
import org.slf4j.LoggerFactory
import java.net.URI

private val logger = LoggerFactory.getLogger("no.nav.helse.soker.SøkerApisKt.søkerApis")

fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    get(SØKER_URL) {
        try {
            call.respond(
                søkerService.getSoker(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        } catch (e: Exception) {
            when (e) {
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(e)
                else -> throw e
            }
        }
    }
}

suspend fun ApplicationCall.respondTilgangNektetProblemDetail(e: TilgangNektetException) = respondProblemDetails(
    logger = logger,
    problemDetails = DefaultProblemDetails(
        title = "tilgangskontroll-feil",
        status = 451,
        instance = URI(request.path()),
        detail = e.message
    )
)
