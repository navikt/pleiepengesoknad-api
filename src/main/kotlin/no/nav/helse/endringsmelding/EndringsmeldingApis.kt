package no.nav.helse.endringsmelding

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.ENDRINGSMELDING_URL
import no.nav.helse.ENDRINGSMELDING_VALIDERING_URL
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getMetadata
import no.nav.helse.innsyn.InnsynGateway
import no.nav.helse.soker.SøkerService
import no.nav.helse.soknad.hentIdTokenOgCallId
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.endringsmelding.EndringsmeldingApisKt")

fun Route.endringsmeldingApis(
    endringsmeldingService: EndringsmeldingService,
    søkerService: SøkerService,
    innsynGateway: InnsynGateway,
    idTokenProvider: IdTokenProvider
) {

    post(ENDRINGSMELDING_URL) {
        val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
        val søker = søkerService.getSoker(idToken, callId)

        logger.info("Mottar og validerer endringsmelding...")
        val søknadsopplysninger = innsynGateway.hentSøknadsopplysninger(idToken, callId)

        val komplettEndringsmelding = call.receive<Endringsmelding>()
            .tilKomplettEndringsmelding(søker)
            .forsikreValidert(søknadsopplysninger.getYtelse<PleiepengerSyktBarn>().søknadsperiode)
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
        val søknadsopplysninger = innsynGateway.hentSøknadsopplysninger(idToken, callId)

        call.receive<Endringsmelding>()
            .tilKomplettEndringsmelding(søker)
            .forsikreValidert(søknadsopplysninger.getYtelse<PleiepengerSyktBarn>().søknadsperiode)

        call.respond(HttpStatusCode.OK)
    }
}

private fun Endringsmelding.tilKomplettEndringsmelding(søker: no.nav.helse.soker.Søker) = KomplettEndringsmelding(
    søker = søker,
    k9Format = Søknad(
        SøknadId(søknadId.toString()),
        Versjon(versjon),
        mottattDato,
        Søker(NorskIdentitetsnummer.of(søker.fødselsnummer)),
        ytelse
    )
)
