package no.nav.helse.barn

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId

class BarnService(
    private val barnGateway: BarnGateway
) {
    private companion object {
        private const val STATUS_DOED = "DØD"
        private const val DISKRESJONSKODE_STRENGT_FORTROLIG_ADRESSE = "SPSF"
        private const val DISKRESJONSKODE_FORTROLIG_ADRESSE = "SPFO"
        private val FILTRER_BORT_DISKRESJONSKODER = listOf(
            DISKRESJONSKODE_FORTROLIG_ADRESSE,
            DISKRESJONSKODE_STRENGT_FORTROLIG_ADRESSE
        )
    }

    internal suspend fun hentNaaverendeBarn(
        norskIdent: NorskIdent,
        callId: CallId
    ) =
        barnGateway.hentBarn(
            norskIdent = norskIdent,
            callId = callId)
            .filter { it.status.toUpperCase() != STATUS_DOED }
            .filter { !FILTRER_BORT_DISKRESJONSKODER.contains(it.diskresjonskode?.toUpperCase()) }
}