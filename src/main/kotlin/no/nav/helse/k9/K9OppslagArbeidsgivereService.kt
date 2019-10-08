package no.nav.helse.k9

import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class K9OppslagArbeidsgivereService (
    private val k9OppslagGateway: K9OppslagGateway
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9OppslagArbeidsgivereService::class.java)
    }

    suspend fun getArbeidsgivere(
        ident: String,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Organisasjon> {
        return try {
            k9OppslagGateway.hentArbeidsgivere(ident, callId, fraOgMed, tilOgMed)
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
            emptyList()
        }
    }
}