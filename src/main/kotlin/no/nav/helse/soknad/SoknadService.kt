package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.soker.Soker
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.validate
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime


class SoknadService(private val pleiepengesoknadMottakGateway: PleiepengesoknadMottakGateway,
                    private val sokerService: SokerService,
                    private val vedleggService: VedleggService) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SoknadService::class.java)
    }

    suspend fun registrer(
        soknad: Soknad,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.trace("Registrerer søknad. Henter søker")
        val soker: Soker = sokerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer om søkeren.")
        soker.validate()

        logger.trace("Validert Søker. Henter ${soknad.vedlegg().size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = soknad.vedlegg(),
            callId = callId
        )

        logger.trace("Vedlegg hentet. Validerer vedleggene.")
        vedlegg.validerVedlegg(soknad.vedlegg())

        logger.trace("Legger søknad til prosessering")

        val komplettSoknad = KomplettSoknad(
            sprak = soknad.sprak,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = soker,
            barn = BarnDetaljer(
                fodselsnummer = soknad.barn.fodselsnummer,
                alternativId = soknad.barn.alternativId,
                aktoerId = soknad.barn.aktoerId,
                navn = soknad.barn.navn
            ),
            vedlegg = vedlegg,
            arbeidsgivere = soknad.arbeidsgivere,
            medlemskap = soknad.medlemskap,
            relasjonTilBarnet = soknad.relasjon(),
            grad = soknad.grad,
            harMedsoker = soknad.harMedsoker!!,
            samtidigHjemme = soknad.samtidigHjemme,
            harBekreftetOpplysninger = soknad.harBekreftetOpplysninger,
            harForstattRettigheterOgPlikter = soknad.harForstattRettigheterOgPlikter,
            dagerPerUkeBorteFraJobb = soknad.dagerPerUkeBorteFraJobb,
            tilsynsordning = soknad.tilsynsordning,
            nattevaak = soknad.nattevaak,
            beredskap = soknad.beredskap
        )

        pleiepengesoknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedleg(
            vedleggUrls = soknad.vedlegg(),
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}

private fun Soknad.relasjon() = if (relasjonTilBarnet.isNullOrBlank()) "Forelder" else relasjonTilBarnet
