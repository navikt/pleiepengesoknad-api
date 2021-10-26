package no.nav.helse.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.ENDRINGSMELDING_URL
import no.nav.helse.ENDRINGSMELDING_VALIDERING_URL
import no.nav.helse.SOKNAD_VALIDERING_URL
import no.nav.helse.SØKNAD_URL
import no.nav.helse.barn.BarnService
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.validate
import no.nav.helse.somJson
import no.nav.helse.vedlegg.DokumentEier
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import no.nav.k9.søknad.felles.Feil
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
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
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)

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

        // TODO: 29/09/2021 OBS MÅ FJERNES FØR PRODSETTING
        logger.info("SKAL IKKE VISES I PROD: K9Format: ${k9FormatSøknad.somJson()}")

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

    post(ENDRINGSMELDING_URL) {
        val endringsMelding = call.receive<no.nav.k9.søknad.Søknad>()
        val k9FormatValideringsFeil: MutableList<Feil> = PleiepengerSyktBarnSøknadValidator().valider(endringsMelding)
        if (k9FormatValideringsFeil.isNotEmpty()) throw Throwblem(
            problemDetails = ValidationProblemDetails(
                violations = k9FormatValideringsFeil.map {
                    Violation(
                        parameterName = it.felt,
                        parameterType = ParameterType.ENTITY,
                        reason = it.feilmelding,
                        invalidValue = "K9-format feilkode: ${it.feilkode}",
                    )
                }
                    .sortedBy { it.reason }.toMutableSet()
            )
        )

        call.respond(HttpStatusCode.Accepted)
    }

    post(ENDRINGSMELDING_VALIDERING_URL) {
        val endringsMelding = call.receive<no.nav.k9.søknad.Søknad>()
        val k9FormatValideringsFeil: MutableList<Feil> = PleiepengerSyktBarnSøknadValidator().valider(endringsMelding)
        if (k9FormatValideringsFeil.isNotEmpty()) throw Throwblem(
            problemDetails = ValidationProblemDetails(
                violations = k9FormatValideringsFeil.map {
                    Violation(
                        parameterName = it.felt,
                        parameterType = ParameterType.ENTITY,
                        reason = it.feilmelding,
                        invalidValue = "K9-format feilkode: ${it.feilkode}",
                    )
                }
                    .sortedBy { it.reason }.toMutableSet()
            )
        )

        call.respond(HttpStatusCode.OK)
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
