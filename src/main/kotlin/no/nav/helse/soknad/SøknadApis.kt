package no.nav.helse.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.SOKNAD_VALIDERING_URL
import no.nav.helse.SØKNAD_URL
import no.nav.helse.barn.BarnService
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.general.CallId
import no.nav.helse.general.formaterStatuslogging
import no.nav.helse.general.getCallId
import no.nav.helse.general.getMetadata
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.vedlegg.DokumentEier
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

fun Route.soknadApis(
    søknadService: SøknadService,
    idTokenProvider: IdTokenProvider,
    barnService: BarnService,
    søkerService: SøkerService,
    vedleggService: VedleggService
) {

    post(SØKNAD_URL) {
        val søknad = call.receive<Søknad>()
        logger.info(formaterStatuslogging(søknad.søknadId, "mottatt"))

        søknadService.registrer(
            søknad = søknad,
            callId = call.getCallId(),
            metadata = call.getMetadata(),
            idToken = idTokenProvider.getIdToken(call)
        )

        call.respond(HttpStatusCode.Accepted)
    }


    post(SOKNAD_VALIDERING_URL) {
        val søknad = call.receive<Søknad>()
        logger.trace("Validerer søknad...")
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)

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

fun ApplicationCall.hentIdTokenOgCallId(idTokenProvider: IdTokenProvider): Pair<IdToken, CallId> =
    Pair(idTokenProvider.getIdToken(this), getCallId())
