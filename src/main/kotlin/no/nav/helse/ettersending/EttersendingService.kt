package no.nav.helse.ettersending

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.soker.Soker
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.validate
import no.nav.helse.soknad.PleiepengesoknadMottakGateway
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EttersendingService(
    private val pleiepengesoknadMottakGateway: PleiepengesoknadMottakGateway,
    private val søkerService: SokerService,
    private val vedleggService: VedleggService
){
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EttersendingService::class.java)
    }

    suspend fun registrer(
        ettersending: Ettersending,
        idToken: IdToken,
        callId: CallId
    ){
        logger.info("Registrerer ettersending. Henter søker")
        val søker: Soker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.info("Søker hentet. Validerer søker.")
        søker.validate()
        logger.info("Søker Validert.")

        logger.trace("Henter ${ettersending.vedlegg.size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = ettersending.vedlegg,
            callId = callId
        )

        logger.trace("Vedlegg hentet. Validerer vedlegg.")
        vedlegg.validerVedlegg(ettersending.vedlegg)
        logger.info("Vedlegg validert")

        logger.info("Legger ettersending til prosessering")

        val komplettEttersending = KomplettEttersending(
            soker = søker,
            sprak = ettersending.sprak,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            vedlegg = vedlegg,
            harForstattRettigheterOgPlikter = ettersending.harForstattRettigheterOgPlikter,
            harBekreftetOpplysninger = ettersending.harBekreftetOpplysninger,
            beskrivelse = ettersending.beskrivelse,
            soknadstype = ettersending.soknadstype
        )

        pleiepengesoknadMottakGateway.leggTilProsesseringEttersending(
            ettersending = komplettEttersending,
            callId = callId
        )

        logger.trace("Ettersending lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedlegg(
            vedleggUrls = ettersending.vedlegg,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}
