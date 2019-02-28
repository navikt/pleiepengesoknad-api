package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.soker.SokerService
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.SoknadService")

class SoknadService(val pleiepengesoknadProsesseringGateway: PleiepengesoknadProsesseringGateway,
                    val sokerService: SokerService,
                    val vedleggService: VedleggService) {

    suspend fun registrer(
        soknad: Soknad,
        fnr: Fodselsnummer,
        callId: CallId
    ) {

        logger.trace("Registrerer søknad. Henter søker")
        val soker = sokerService.getSoker(fnr = fnr, callId = callId)
        logger.trace("Søker hentet. Henter vedlegg")
        val vedlegg = vedleggService.hentVedlegg(
            vedleggUrls = soknad.vedlegg,
            fnr = fnr
        )
        logger.trace("Vedlegg hentet. Legger søknad til prosessering")

        val komplettSoknad = KomplettSoknad(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = soker,
            barn = soknad.barn,
            vedlegg = vedlegg,
            arbeidsgivere = soknad.arbeidsgivere,
            medlemskap = soknad.medlemskap,
            relasjonTilBarnet = soknad.relasjonTilBarnet
        )

//        pleiepengesoknadProsesseringGateway.leggTilProsessering(
//            soknad = komplettSoknad,
//            callId = callId
//        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedleg(
            vedleggUrls = soknad.vedlegg,
            fnr = fnr
        )

        logger.trace("Vedlegg slettet.")
    }
}