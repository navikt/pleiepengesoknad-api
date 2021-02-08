package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.validate
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.ValideringsFeil
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime


class SøknadService(
    private val pleiepengesoknadMottakGateway: PleiepengesoknadMottakGateway,
    private val vedleggService: VedleggService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        k9FormatSøknad: no.nav.k9.søknad.Søknad,
        søker: Søker,
        mottatt: ZonedDateTime
    ) {
        logger.info("Registrerer søknad")
        logger.trace("Henter ${søknad.vedlegg.size} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId
        )

        logger.trace("Vedlegg hentet. Validerer vedleggene.")
        vedlegg.validerVedlegg(søknad.vedlegg)

        logger.trace("Legger søknad til prosessering")
        val komplettSøknad = KomplettSøknad(
            språk = søknad.språk,
            søknadId = søknad.søknadId,
            mottatt = mottatt,
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
            beskrivelseOmsorgsrollen = søknad.beskrivelseOmsorgsrollen,
            barnRelasjon = søknad.barnRelasjon,
            barnRelasjonBeskrivelse = søknad.barnRelasjonBeskrivelse,
            k9FormatSøknad = k9FormatSøknad
        )

        logger.info(
            "K9Format = {}",
            JsonUtils.toString(komplettSøknad.k9FormatSøknad)
        ) //TODO For test, fjernes før prodsetting

        pleiepengesoknadMottakGateway.leggTilProsessering(
            søknad = komplettSøknad,
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