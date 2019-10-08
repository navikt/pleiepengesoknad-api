package no.nav.helse.k9

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class K9OppslagBarnService(
    private val k9OppslagGateway: K9OppslagGateway
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9OppslagBarnService::class.java)

        private const val STATUS_DOED = "DØD"
        private const val DISKRESJONSKODE_STRENGT_FORTROLIG_ADRESSE = "SPSF"
        private const val DISKRESJONSKODE_FORTROLIG_ADRESSE = "SPFO"
        private val FILTRER_BORT_DISKRESJONSKODER = listOf(
            DISKRESJONSKODE_FORTROLIG_ADRESSE,
            DISKRESJONSKODE_STRENGT_FORTROLIG_ADRESSE
        )
    }

    internal suspend fun hentNaaverendeBarn(
        ident: String,
        callId: CallId
    ) =
        try {
            k9OppslagGateway.hentBarn(
                personIdent = ident,
                callId = callId
            )
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av barn, returnerer en tom liste", cause)
            emptyList<Barn>()
        }
}