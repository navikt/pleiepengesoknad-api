package no.nav.helse.barn

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BarnService(
    private val barnGateway: BarnGateway
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(BarnService::class.java)

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
        try {
            barnGateway.hentBarn(
                norskIdent = norskIdent,
                callId = callId
            )
                .filter { it.status.toUpperCase() != STATUS_DOED }
                .filter { !FILTRER_BORT_DISKRESJONSKODER.contains(it.diskresjonskode?.toUpperCase()) }
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av barn, returnerer en tom liste", cause)
            emptyList<Barn>()
        }
}