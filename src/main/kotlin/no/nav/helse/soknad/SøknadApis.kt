package no.nav.helse.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.barn.BarnService
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.validate
import no.nav.helse.vedlegg.DokumentEier
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.soknadApis(
    søknadService: SøknadService,
    idTokenProvider: IdTokenProvider,
    barnService: BarnService,
    søkerService: SøkerService,
    vedleggService: VedleggService
) {

    @Location("/soknad")
    class sendSoknad

    post { _ : sendSoknad ->
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()

        logger.trace("Mottatt ny søknad. Mapper søknad.")
        val søknad = call.receive<Søknad>()

        logger.trace("Henter søker.")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)
        logger.trace("Søker hentet. Validerer søkeren.")
        søker.validate()

        logger.trace("Oppdaterer barn med identitetsnummer")
        val listeOverBarnMedFnr = barnService.hentNaaverendeBarn(idTokenProvider.getIdToken(call), call.getCallId())
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)

        logger.info("Mapper om søknad til k9format.")
        val k9FormatSøknad = søknad.tilK9Format(mottatt, søker)

        logger.info("Validerer søknad")
        søknad.validate(k9FormatSøknad)
        logger.trace("Validering OK. Registrerer søknad.")

        søknadService.registrer(
            søknad = søknad,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call),
            k9FormatSøknad = k9FormatSøknad,
            søker = søker,
            mottatt = mottatt
        )

        logger.trace("Søknad registrert.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location("/soknad/valider")
    class validerSoknad

    post { _: validerSoknad ->
        val søknad = call.receive<Søknad>()
        logger.trace("Validerer søknad...")
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        val listeOverBarnMedFnr = barnService.hentNaaverendeBarn(idTokenProvider.getIdToken(call), call.getCallId())
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)

        val k9FormatSøknad = søknad.tilK9Format(mottatt, søker)
        søknad.validate(k9FormatSøknad)

        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId,
            eier = DokumentEier(søker.fødselsnummer)
        )
        vedlegg.validerVedlegg(søknad.vedlegg)

        logger.trace("Validering Ok.")
        call.respond(HttpStatusCode.Accepted)
    }
}
