package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.auth.IdToken
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.validate
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

        logger.trace("Søker hentet. Validerer om søkeren.")
        soker.validate()

        logger.trace("Validert Søker. Henter ${soknad.vedlegg.size} vedlegg")
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
            barn = BarnDetaljer(
                fodselsnummer = soknad.barn.fodselsnummer,
                alternativId = soknad.barn.alternativId,
                aktoerId = soknad.barn.aktoerId,
                navn = barnetsNavn(soknad.barn)
            ),
            vedlegg = vedlegg,
            arbeidsgivere = soknad.arbeidsgivere,
            medlemskap = soknad.medlemskap,
            relasjonTilBarnet = soknad.relasjon(),
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

    private suspend fun barnetsNavn(barn: BarnDetaljer): String? {
        return if (barn.aktoerId == null) barn.navn
        else try {
            // TODO: hente navn.
            null
        } catch (cause: Throwable) {
            logger.error("Feil ved oppslag på barnet", cause)
            null
        }
    }
}

private fun Soknad.relasjon(): String = if (relasjonTilBarnet.isNullOrBlank()) "Forelder" else relasjonTilBarnet
