package no.nav.helse.soker

import com.auth0.jwt.JWT
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken

class SøkerService (
    private val søkerGateway: SøkerGateway
) {
    suspend fun getSoker(
        idToken: IdToken,
        callId: CallId
    ): Søker {
        val ident: String = JWT.decode(idToken.value).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        return søkerGateway.hentSoker(idToken, callId).tilSoker(ident)
    }

    private fun  SøkerGateway.SokerOppslagRespons.tilSoker(fodselsnummer: String) = Søker(
        aktørId = aktør_id,
        fodselsnummer = fodselsnummer, // TODO: Bør skifte til "alternativ_id" ?
        fodselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}