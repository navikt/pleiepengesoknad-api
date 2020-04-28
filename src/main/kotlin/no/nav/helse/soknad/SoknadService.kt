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
        soknad: Soknad,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.trace("Registrerer søknad. Henter søker")
        val soker: Søker = sokerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer om søkeren.")
        soker.validate()

        logger.trace("Validert Søker. Henter ${soknad.vedlegg.size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = soknad.vedlegg,
            callId = callId
        )

        logger.trace("Vedlegg hentet. Validerer vedleggene.")
        vedlegg.validerVedlegg(soknad.vedlegg)

        logger.trace("Legger søknad til prosessering")

        val komplettSoknad = KomplettSoknad(
            sprak = soknad.sprak,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = soker,
            barn = BarnDetaljer(
                fodselsnummer = soknad.barn.fodselsnummer,
                fodselsdato = soknad.barn.fodselsdato,
                aktørId = soknad.barn.aktørId,
                navn = soknad.barn.navn
            ),
            vedlegg = vedlegg,
            arbeidsgivere = soknad.arbeidsgivere,
            medlemskap = soknad.medlemskap,
            bekrefterPeriodeOver8Uker = soknad.bekrefterPeriodeOver8Uker,
            ferieuttakIPerioden = soknad.ferieuttakIPerioden,
            utenlandsoppholdIPerioden = soknad.utenlandsoppholdIPerioden,
            relasjonTilBarnet = soknad.relasjon(),
            harMedsoker = soknad.harMedsoker!!,
            samtidigHjemme = soknad.samtidigHjemme,
            harBekreftetOpplysninger = soknad.harBekreftetOpplysninger,
            harForstattRettigheterOgPlikter = soknad.harForstattRettigheterOgPlikter,
            tilsynsordning = soknad.tilsynsordning,
            nattevaak = soknad.nattevaak,
            beredskap = soknad.beredskap,
            frilans = soknad.frilans,
            selvstendigVirksomheter = soknad.selvstendigVirksomheter,
            skalBekrefteOmsorg = soknad.skalBekrefteOmsorg,
            skalPassePaBarnetIHelePerioden = soknad.skalPassePaBarnetIHelePerioden,
            beskrivelseOmsorgsRollen = soknad.beskrivelseOmsorgsRollen
        )

        pleiepengesoknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedlegg(
            vedleggUrls = soknad.vedlegg,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}

private fun Soknad.relasjon() = if (relasjonTilBarnet.isNullOrBlank()) "Forelder" else relasjonTilBarnet
