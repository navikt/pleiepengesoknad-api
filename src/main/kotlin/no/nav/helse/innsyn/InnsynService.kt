package no.nav.helse.innsyn

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.general.CallId

class InnsynService(
    private val innsynGateway: InnsynGateway
) {

    suspend fun hentSøknadsopplysningerForBarn(
        idToken: IdToken,
        callId: CallId,
        barnAktørId: String
    ): K9SakInnsynSøknad {
        return innsynGateway.hentSøknadsopplysninger(idToken, callId)
            .firstOrNull { k9SakInnsynSøknad: K9SakInnsynSøknad ->
                k9SakInnsynSøknad.barn.aktørId == barnAktørId
            } ?: throw IllegalStateException("Søknadsopplysninger inneholdt ikke riktig barn.")
    }
}
