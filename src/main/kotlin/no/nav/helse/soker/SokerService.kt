package no.nav.helse.soker

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId

class SokerService (
    private val sokerGateway: SokerGateway
) {
    suspend fun getSoker(
        ident: NorskIdent,
        callId: CallId
    ) = sokerGateway.hentSoker(ident, callId).tilSoker(ident)

    private fun  SokerGateway.SokerOppslagRespons.tilSoker(ident: NorskIdent) = Soker(
        aktoerId = aktør_id,
        fodselsnummer = ident.getValue(), // TODO: Bør skifte til "alternativ_id" ?
        fodselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}