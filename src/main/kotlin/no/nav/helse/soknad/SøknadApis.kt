package no.nav.helse.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.barn.BarnService
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.soknadApis(
    søknadService: SøknadService,
    idTokenProvider: IdTokenProvider,
    barnService: BarnService
) {

    @Location("/soknad")
    class sendSoknad

    post { _ : sendSoknad ->
        logger.trace("Mottatt ny søknad. Mapper søknad.")
        val søknad = call.receive<Søknad>()
        logger.trace("Søknad mappet. Validerer")

        logger.trace("Oppdaterer barn med identitetsnummer")
        val listeOverBarnMedFnr = barnService.hentNaaverendeBarn(idTokenProvider.getIdToken(call), call.getCallId())
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)
        logger.info("Oppdatering av identitetsnummer på barn OK")

        logger.info("Debugging newVersion=${søknad.newVersion}")
        søknad.validate()
        logger.trace("Validering OK. Registrerer søknad.")

        søknadService.registrer(
            søknad = søknad,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Søknad registrert.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location("/soknad/valider")
    class validerSoknad

    post { _: validerSoknad ->
        val søknad = call.receive<Søknad>()
        logger.trace("Validerer søknad...")
        søknad.validate()
        logger.trace("Validering Ok.")
        call.respond(HttpStatusCode.Accepted)
    }
}
