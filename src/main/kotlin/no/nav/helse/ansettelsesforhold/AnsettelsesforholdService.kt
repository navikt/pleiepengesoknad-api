package no.nav.helse.ansettelsesforhold

import no.nav.helse.general.auth.Fodselsnummer

class AnsettelsesforholdService(
    private val gateway: AnsettelsesforholdGateway
) {
    suspend fun getAnsettelsesforhold(fnr: Fodselsnummer) : List<Ansettelsesforhold> {
        return gateway.getAnsettelsesforhold(fnr)
    }
}