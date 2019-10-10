package no.nav.helse.soker

import no.nav.helse.general.CallId

class SokerService (
    private val sokerGateway: SokerGateway
) {
    suspend fun getSoker(
        ident: String,
        callId: CallId
    ) = sokerGateway.hentSoker(ident, callId).tilSoker(ident)

    private fun  SokerGateway.SokerOppslagRespons.tilSoker(ident: String) = Soker(
        aktoerId = aktør_id,
        fodselsnummer = ident, // TODO: Bør skifte til "alternativ_id" ?
        fodselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}