package no.nav.helse.barn

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer


class BarnService(
    private val barnGateway: BarnGateway
) {
    private companion object {
        private const val STATUS_DOED = "DØD"
    }

    suspend fun hentNaaverendeBarn(
        fnr: Fodselsnummer,
        callId: CallId
    ) =
        barnGateway.hentBarn(
            fnr = fnr,
            callId = callId
        ).filter { it.status != STATUS_DOED }
}