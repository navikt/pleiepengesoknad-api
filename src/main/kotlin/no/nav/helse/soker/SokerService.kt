package no.nav.helse.soker

import com.auth0.jwt.JWT
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken

class SokerService (
    private val sokerGateway: SokerGateway
) {
    suspend fun getSoker(
        idToken: IdToken,
        callId: CallId
    ): Soker {
        val ident: String = JWT.decode(idToken.value).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        return sokerGateway.hentSoker(idToken, callId).tilSoker(ident)
    }

    private fun  SokerGateway.SokerOppslagRespons.tilSoker(fodselsnummer: String) = Soker(
        aktoerId = aktør_id,
        fodselsnummer = fodselsnummer, // TODO: Bør skifte til "alternativ_id" ?
        fodselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}