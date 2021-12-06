package no.nav.helse.endringsmelding

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.ENDRINGSMELDING_URL
import no.nav.helse.ENDRINGSMELDING_VALIDERING_URL
import no.nav.helse.barn.BarnService
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getMetadata
import no.nav.helse.innsyn.InnsynGateway
import no.nav.helse.innsyn.K9SakInnsynSøknad
import no.nav.helse.soker.SøkerService
import no.nav.helse.soknad.hentIdTokenOgCallId
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.endringsmelding.EndringsmeldingApisKt")

fun Route.endringsmeldingApis(
    endringsmeldingService: EndringsmeldingService,
    søkerService: SøkerService,
    barnService: BarnService,
    innsynGateway: InnsynGateway,
    idTokenProvider: IdTokenProvider
) {

    post(ENDRINGSMELDING_URL) {
        val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
        val søker = søkerService.getSoker(idToken, callId)
        val barnListe = barnService.hentNaaverendeBarn(idToken, callId)
        val endringsmelding = call.receive<Endringsmelding>()
        val endringsmeldingBarn = endringsmelding.ytelse.barn

        logger.info("Mottar og validerer endringsmelding...")
        val søknadsopplysninger: K9SakInnsynSøknad = innsynGateway.hentSøknadsopplysninger(idToken, callId)
            .firstOrNull { k9SakInnsynSøknad: K9SakInnsynSøknad ->
                barnListe.firstOrNull { it.identitetsnummer == endringsmeldingBarn.personIdent.verdi } != null
            } ?: throw IllegalStateException("Søknadsopplysninger inneholdt ikke riktig barn.")

        val ytelse = søknadsopplysninger.søknad.getYtelse<PleiepengerSyktBarn>()

        val komplettEndringsmelding = endringsmelding
            .tilKomplettEndringsmelding(søker)
            .forsikreValidert(ytelse.søknadsperiode)
        logger.info("Endringsmelding validert.")

        endringsmeldingService.registrer(
            komplettEndringsmelding = komplettEndringsmelding,
            metadata = call.getMetadata()
        )

        call.respond(HttpStatusCode.Accepted)
    }

    post(ENDRINGSMELDING_VALIDERING_URL) {
        val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
        val søker = søkerService.getSoker(idToken, callId)
        val barnListe = barnService.hentNaaverendeBarn(idToken, callId)

        logger.info("Mottar og validerer endringsmelding...")
        val søknadsopplysninger: K9SakInnsynSøknad = innsynGateway.hentSøknadsopplysninger(idToken, callId)
            .firstOrNull { k9SakInnsynSøknad: K9SakInnsynSøknad ->
                barnListe.firstOrNull { barn: no.nav.helse.barn.Barn ->
                    barn.identitetsnummer == k9SakInnsynSøknad.søknad.getYtelse<PleiepengerSyktBarn>().barn.personIdent.verdi
                } != null
            } ?: throw IllegalStateException("Søknadsopplysninger inneholdt ikke forventet opplysninger om barn.")

        call.receive<Endringsmelding>()
            .tilKomplettEndringsmelding(søker)
            .forsikreValidert(søknadsopplysninger.søknad.getYtelse<PleiepengerSyktBarn>().søknadsperiode)

        call.respond(HttpStatusCode.OK)
    }
}

private fun Endringsmelding.tilKomplettEndringsmelding(
    søker: no.nav.helse.soker.Søker
): KomplettEndringsmelding {
    return KomplettEndringsmelding(
        søker = søker,
        harBekreftetOpplysninger = harBekreftetOpplysninger,
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
        k9Format = Søknad(
            søknadId?.let { SøknadId(it.toString()) } ?: SøknadId(UUID.randomUUID().toString()),
            Versjon("1.0.0"),
            mottattDato,
            Søker(NorskIdentitetsnummer.of(søker.fødselsnummer)),
            Språk.of(språk),
            ytelse
        )
    )
}
