package no.nav.helse.ansettelsesforhold

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import java.time.LocalDate

class AnsettelsesforholdService(
    private val gateway: AnsettelsesforholdGateway
) {
    suspend fun getAnsettelsesforhold(
        fnr: Fodselsnummer,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Ansettelsesforhold> {
        return gateway.getAnsettelsesforhold(fnr, callId, fraOgMed, tilOgMed)
    }
}