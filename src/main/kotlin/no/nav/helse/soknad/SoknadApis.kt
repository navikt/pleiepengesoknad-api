package no.nav.helse.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.routing.Route
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.getCallId
import no.nav.helse.general.validation.ValidationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.soknadApis(
    validationHandler: ValidationHandler,
    soknadService: SoknadService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/soknad")
    class sendSoknad

    post { _ : sendSoknad ->
        logger.trace("Mottatt ny søknad. Mapper søknad.")
        val soknad = call.receive<Soknad>()
        logger.trace("Søknad mappet. Validerer")
        validationHandler.validate(soknad)
        logger.trace("Validering OK. Registrerer søknad.")

        soknadService.registrer(
            soknad = soknad,
            fnr = call.getFodselsnummer(),
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Søknad registrert.")
        call.response.status(HttpStatusCode.Accepted)
    }
}