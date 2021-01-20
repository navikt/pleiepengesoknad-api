package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.validate
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime


class SoknadService(private val pleiepengesoknadMottakGateway: PleiepengesoknadMottakGateway,
                    private val sokerService: SøkerService,
                    private val vedleggService: VedleggService) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SoknadService::class.java)
    }

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.trace("Registrerer søknad. Henter søker")
        val søker: Søker = sokerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer om søkeren.")
        søker.validate()

        logger.trace("Validert Søker. Henter ${søknad.vedlegg.size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId
        )

        logger.trace("Vedlegg hentet. Validerer vedleggene.")
        vedlegg.validerVedlegg(søknad.vedlegg)

        logger.trace("Legger søknad til prosessering")

        val komplettSoknad = KomplettSøknad(
            språk = søknad.språk,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            fraOgMed = søknad.fraOgMed,
            tilOgMed = søknad.tilOgMed,
            søker = søker,
            barn = BarnDetaljer(
                fødselsnummer = søknad.barn.fødselsnummer,
                fødselsdato = søknad.barn.fødselsdato,
                aktørId = søknad.barn.aktørId,
                navn = søknad.barn.navn
            ),
            vedlegg = vedlegg,
            arbeidsgivere = søknad.arbeidsgivere,
            medlemskap = søknad.medlemskap,
            bekrefterPeriodeOver8Uker = søknad.bekrefterPeriodeOver8Uker,
            ferieuttakIPerioden = søknad.ferieuttakIPerioden,
            utenlandsoppholdIPerioden = søknad.utenlandsoppholdIPerioden,
            harMedsøker = søknad.harMedsøker!!,
            samtidigHjemme = søknad.samtidigHjemme,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter,
            tilsynsordning = søknad.tilsynsordning,
            nattevåk = søknad.nattevåk,
            beredskap = søknad.beredskap,
            frilans = søknad.frilans,
            selvstendigVirksomheter = søknad.selvstendigVirksomheter,
            skalBekrefteOmsorg = søknad.skalBekrefteOmsorg,
            skalPassePåBarnetIHelePerioden = søknad.skalPassePåBarnetIHelePerioden,
            beskrivelseOmsorgsrollen = søknad.beskrivelseOmsorgsrollen
        )

        pleiepengesoknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedlegg(
            vedleggUrls = søknad.vedlegg,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}