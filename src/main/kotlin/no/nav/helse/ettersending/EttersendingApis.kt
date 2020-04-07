package no.nav.helse.ettersending

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.ettersendingApis(
    ettersendingService: EttersendingService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/ettersending/valider")
    class validerEttersending

    post { _: validerEttersending ->
        val ettersending = call.receive<Ettersending>()
        logger.trace("Validerer ettersending...")
        ettersending.valider()
        logger.trace("Validering Ok.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location("/ettersend")
    class sendEttersending

    post { _ : sendEttersending ->
        logger.trace("Mottatt ettersending. Mapper ettersending.")
        val ettersending = call.receive<Ettersending>()
        logger.trace("Ettersending mappet. Validerer")

        ettersending.valider()
        logger.trace("Validering OK. Registrerer ettersending.")

        ettersendingService.registrer(
            ettersending = ettersending,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Ettersending registrert.")
        call.respond(HttpStatusCode.Accepted)
    }
}
