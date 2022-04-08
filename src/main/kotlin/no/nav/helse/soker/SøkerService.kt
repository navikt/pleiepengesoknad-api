package no.nav.helse.soker

import com.auth0.jwt.JWT
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.general.CallId

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
        fødselsnummer = fodselsnummer, // TODO: Bør skifte til "alternativ_id" ?
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}
