package no.nav.helse.arbeidsgiver

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import java.time.LocalDate

class ArbeidsgiverService(
    private val gateway: ArbeidsgiverGateway
) {
    suspend fun getAnsettelsesforhold(
        fnr: Fodselsnummer,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Arbeidsgiver> {
        return gateway.getAnsettelsesforhold(fnr, callId, fraOgMed, tilOgMed)
    }
}