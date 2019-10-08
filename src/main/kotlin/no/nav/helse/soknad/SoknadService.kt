package no.nav.helse.soknad

import no.nav.helse.aktoer.*
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import no.nav.helse.k9.K9OppslagSokerService
import no.nav.helse.soker.Soker
import no.nav.helse.soker.validate
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime


class SoknadService(private val pleiepengesoknadMottakGateway: PleiepengesoknadMottakGateway,
                    private val k9OppslagSokerService: K9OppslagSokerService,
                    private val aktoerService: AktoerService,
                    private val vedleggService: VedleggService) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SoknadService::class.java)
    }

    suspend fun registrer(
        soknad: Soknad,
        norskIdent: NorskIdent,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.trace("Registrerer søknad. Henter søker")
        val soker = k9OppslagSokerService.getSoker(ident = norskIdent.getValue(), callId = callId)

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

        logger.trace("Henter barnets norske ident")
        val barnetsNorskeIdent = barnetsNorskeIdent(soknad.barn, callId)

        logger.trace("Legger søknad til prosessering")

        val komplettSoknad = KomplettSoknad(
            sprak = soknad.sprak,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = soker,
            barn = BarnDetaljer(
                fodselsnummer = if (barnetsNorskeIdent is Fodselsnummer) barnetsNorskeIdent.getValue() else null,
                alternativId = if (barnetsNorskeIdent is AlternativId) barnetsNorskeIdent.getValue() else null,
                aktoerId = soknad.barn.aktoerId,
                navn = barnetsNavn(soknad.barn, callId)
            ),
            vedlegg = vedlegg,
            arbeidsgivere = soknad.arbeidsgivere,
            medlemskap = soknad.medlemskap,
            relasjonTilBarnet = soknad.relasjon(),
            grad = soknad.grad,
            harMedsoker = soknad.harMedsoker!!,
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
            vedleggUrls = soknad.vedlegg,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }

    private suspend fun barnetsNorskeIdent(barn: BarnDetaljer, callId: CallId) : NorskIdent? {
        return when {
            barn.fodselsnummer != null -> Fodselsnummer(barn.fodselsnummer)
            barn.alternativId != null -> AlternativId(barn.alternativId)
            barn.aktoerId != null -> {
                try {
                    aktoerService.getNorskIdent(
                        aktoerId = (AktoerId(barn.aktoerId)),
                        callId = callId
                    )
                } catch (cause: Throwable) {
                    logger.error("Feil på oppslag på barnets norske ident.", cause)
                    null
                }
            }
            else -> null
        }
    }

    private suspend fun barnetsNavn(barn: BarnDetaljer, callId: CallId): String? {
        return barn.navn ?: if (barn.fodselsnummer != null) try {
            k9OppslagSokerService.getSoker(
                ident = barn.fodselsnummer,
                callId = callId
            ).sammensattNavn()
        } catch (cause: Throwable) {
            logger.error("Feil ved oppslag på barnets navn.", cause)
            null
        } else null
    }
}

private fun Soker.sammensattNavn() = if (mellomnavn == null) "$fornavn $etternavn" else "$fornavn $mellomnavn $etternavn"
private fun Soknad.relasjon() = if (relasjonTilBarnet.isNullOrBlank()) "Forelder" else relasjonTilBarnet
