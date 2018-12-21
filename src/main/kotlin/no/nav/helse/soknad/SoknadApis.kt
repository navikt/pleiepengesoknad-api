package no.nav.helse.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.validation.ValidationException
import no.nav.helse.general.validation.ValidationHandler
import no.nav.helse.general.validation.Violation
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
        validateDates(soknad)

        soknadService.registrer(
            soknad = soknad,
            fnr = getFodselsnummer(call)
        )

        call.response.status(HttpStatusCode.Accepted)
    }
}

// TODO: Kan løses med en custom validerings-annotasjon på Soknad
private fun validateDates(soknad: Soknad) {
    if (soknad.tilOgMed.isBefore(soknad.fraOgMed)) {
        throw ValidationException(listOf(
            Violation(
                name = "til_og_med og fra_og_med",
                reason = "til_og_med kan ikke være før fra_og_til"
        )))
    }
}