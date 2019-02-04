package no.nav.helse.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.getCallId
import no.nav.helse.general.validation.ValidationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")


@KtorExperimentalLocationsAPI
fun Route.soknadApis(
    validationHandler: ValidationHandler,
    soknadService: SoknadService
) {

    @Location("/soknad")
    class sendSoknad

    post { _ : sendSoknad ->
        val soknad = call.receive<Soknad>()

        validationHandler.validate(soknad)

        soknadService.registrer(
            soknad = soknad,
            fnr = call.getFodselsnummer(),
            callId = call.getCallId()
        )

        call.response.status(HttpStatusCode.Accepted)
    }
}