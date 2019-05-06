package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.auth.IdToken
import no.nav.helse.soker.SokerService
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.SoknadService")

class SoknadService(val pleiepengesoknadProsesseringGateway: PleiepengesoknadProsesseringGateway,
                    val sokerService: SokerService,
                    val vedleggService: VedleggService) {

    suspend fun registrer(
        soknad: Soknad,
        fnr: Fodselsnummer,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.trace("Registrerer søknad. Henter søker")
        val soker = sokerService.getSoker(fnr = fnr, callId = callId)
        logger.trace("Søker hentet. Henter ${soknad.vedlegg.size} vedlegg")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = soknad.vedlegg,
            callId = callId
        )
        logger.trace("Validerer totale størreslen på vedleggene.")
        vedlegg.validerTotalStorresle()

        logger.trace("Vedlegg hentet. Legger søknad til prosessering")
        if (soknad.vedlegg.size != vedlegg.size) {
            logger.warn("Mottok referanse til ${soknad.vedlegg.size} vedlegg, men fant bare ${vedlegg.size} som sendes til prosessering.")
        }

        val komplettSoknad = KomplettSoknad(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = soker,
            barn = soknad.barn,
            vedlegg = vedlegg,
            arbeidsgivere = soknad.arbeidsgivere,
            medlemskap = soknad.medlemskap,
            relasjonTilBarnet = soknad.relasjonTilBarnet,
            grad = soknad.grad,
            harMedsoker = soknad.harMedsoker!!,
            harBekreftetOpplysninger = soknad.harBekreftetOpplysninger,
            harForstattRettigheterOgPlikter = soknad.harForstattRettigheterOgPlikter
        )

        pleiepengesoknadProsesseringGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedleg(
            vedleggUrls = soknad.vedlegg,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}