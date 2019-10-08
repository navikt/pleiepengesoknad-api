package no.nav.helse.k9

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import no.nav.helse.soker.Soker

class K9OppslagSokerService (
    private val k9OppslagGateway: K9OppslagGateway
) {
    suspend fun getSoker(
        ident: String,
        callId: CallId
    ): Soker {
        val sokerOppslagRespons: SokerOppslagRespons = k9OppslagGateway.hentSoker(ident, callId)
        return Soker(
            aktoerId = sokerOppslagRespons.aktør_id,
            fodselsnummer = ident, // TODO: Bør skifte til "alternativ_id" ?
            fodselsdato = sokerOppslagRespons.fødselsdato,
            fornavn = sokerOppslagRespons.fornavn,
            mellomnavn = sokerOppslagRespons.mellomnavn,
            etternavn = sokerOppslagRespons.etternavn
        )
    }
}