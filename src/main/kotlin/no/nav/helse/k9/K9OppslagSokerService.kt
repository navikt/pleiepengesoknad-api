package no.nav.helse.k9

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import no.nav.helse.soker.Soker

class K9OppslagSokerService (
    private val k9OppslagGateway: K9OppslagGateway
) {
    suspend fun getSoker(
        norskIdent: NorskIdent,
        callId: CallId
    ): Soker {
        val sokerOppslagRespons: SokerOppslagRespons = k9OppslagGateway.hentSoker(norskIdent.getValue(), callId)
        return Soker(
            aktoerId = sokerOppslagRespons.aktør_id!!,
            fodselsnummer = norskIdent.getValue(), // TODO: Bør skifte til "alternativ_id" ?
            fodselsdato = sokerOppslagRespons.fødselsdato,
            fornavn = sokerOppslagRespons.fornavn,
            mellomnavn = sokerOppslagRespons.mellomnavn,
            etternavn = sokerOppslagRespons.etternavn
        )
    }
}