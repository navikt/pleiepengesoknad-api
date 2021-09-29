package no.nav.helse.soknad

import no.nav.helse.PleiepengesoknadMottakGateway
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.soker.Søker
import no.nav.helse.vedlegg.DokumentEier
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
            callId = callId,
            eier = DokumentEier(søker.fødselsnummer)
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
            ferieuttakIPerioden = søknad.ferieuttakIPerioden,
            utenlandsoppholdIPerioden = søknad.utenlandsoppholdIPerioden,
            harMedsøker = søknad.harMedsøker!!,
            samtidigHjemme = søknad.samtidigHjemme,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter,
            omsorgstilbud = søknad.omsorgstilbud,
            nattevåk = søknad.nattevåk,
            beredskap = søknad.beredskap,
            frilans = søknad.frilans,
            selvstendigNæringsdrivende = søknad.selvstendigNæringsdrivende,
            barnRelasjon = søknad.barnRelasjon,
            barnRelasjonBeskrivelse = søknad.barnRelasjonBeskrivelse,
            harVærtEllerErVernepliktig = søknad.harVærtEllerErVernepliktig,
            k9FormatSøknad = k9FormatSøknad
        )

        pleiepengesoknadMottakGateway.leggTilProsessering(
            søknad = komplettSøknad,
            callId = callId
        )

        logger.trace("Søknad sendt til mottak.")
    }
}
