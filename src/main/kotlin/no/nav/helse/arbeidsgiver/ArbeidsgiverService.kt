package no.nav.helse.arbeidsgiver

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class ArbeidsgiverService(
    private val gateway: ArbeidsgiverGateway
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidsgiverService::class.java)
    }

    suspend fun getArbeidsgivere(
        norskIdent: NorskIdent,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Arbeidsgiver> {
        return try { gateway.getArbeidsgivere(norskIdent, callId, fraOgMed, tilOgMed) } catch (cause: Throwable) {
            logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
            emptyList()
        }
    }
}