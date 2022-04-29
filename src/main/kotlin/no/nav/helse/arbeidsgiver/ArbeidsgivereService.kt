package no.nav.helse.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.general.CallId
import no.nav.helse.general.oppslag.TilgangNektetException
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class ArbeidsgivereService(
    private val arbeidsgivereGateway: ArbeidsgivereGateway
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidsgivereService::class.java)
    }

    suspend fun getArbeidsgivere(
        idToken: IdToken,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean
    ): Arbeidsgivere {
        return try {
            arbeidsgivereGateway.hentArbeidsgivere(idToken, callId, fraOgMed, tilOgMed, skalHentePrivateArbeidsgivere, skalHenteFrilansoppdrag)
        } catch (cause: Throwable) {
            when (cause) {
                is TilgangNektetException -> throw cause
                else -> {
                    logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
                    Arbeidsgivere(emptyList(), emptyList(), emptyList())
                }
            }
        }
    }

    suspend fun hentOrganisasjoner(
        idToken: IdToken,
        callId: CallId,
        organisasjoner: Set<Organisasjonsnummer>
    ) : Arbeidsgivere {
        return try {
            arbeidsgivereGateway.hentArbeidsgivere(idToken, callId, organisasjoner)
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
            Arbeidsgivere(emptyList(), emptyList(), emptyList())
        }
    }
}
