package no.nav.helse.arbeidsgiver

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import java.time.LocalDate

class ArbeidsgiverService(
    private val gateway: ArbeidsgiverGateway
) {
    suspend fun getAnsettelsesforhold(
        norskIdent: NorskIdent,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Arbeidsgiver> {
        return gateway.getAnsettelsesforhold(norskIdent, callId, fraOgMed, tilOgMed)
    }
}