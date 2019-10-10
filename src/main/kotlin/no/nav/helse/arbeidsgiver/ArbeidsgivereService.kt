package no.nav.helse.arbeidsgiver

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class ArbeidsgivereService (
    private val arbeidsgivereGateway: ArbeidsgivereGateway
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidsgivereService::class.java)
    }

    suspend fun getArbeidsgivere(
        ident: NorskIdent,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Organisasjon> {
        return try {
            arbeidsgivereGateway.hentArbeidsgivere(ident, callId, fraOgMed, tilOgMed)
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
            emptyList()
        }
    }
}