package no.nav.helse.soker

import no.nav.helse.general.CallId
import no.nav.helse.general.oppslag.K9OppslagGateway

class SokerService (
    private val sokerGateway: SokerGateway
) {
    suspend fun getSoker(
        ident: String,
        callId: CallId
    ): Soker {
        val sokerOppslagRespons: SokerOppslagRespons = sokerGateway.hentSoker(ident, callId)
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