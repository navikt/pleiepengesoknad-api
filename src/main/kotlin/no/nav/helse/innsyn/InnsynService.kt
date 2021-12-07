package no.nav.helse.innsyn

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken

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
                k9SakInnsynSøknad.barn.aktør_id == barnAktørId
            } ?: throw IllegalStateException("Søknadsopplysninger inneholdt ikke riktig barn.")
    }
}
