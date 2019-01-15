package no.nav.helse.ansettelsesforhold

import io.prometheus.client.Counter
import no.nav.helse.general.auth.Fodselsnummer

private val ansettelsesforholdOppslagCounter = Counter.build()
    .name("oppslag_ansettelsesforhold")
    .help("Antall oppslag gjort for arbeidsforhold på person")
    .labelNames("status")
    .register()

class AnsettelsesforholdService(
    private val gateway: AnsettelsesforholdGateway
) {
    suspend fun getAnsettelsesforhold(fnr: Fodselsnummer) : List<Ansettelsesforhold> {
        try {
            val ansettelsesforhold = gateway.getAnsettelsesforhold(fnr)
            ansettelsesforholdOppslagCounter.labels("success").inc()
            return ansettelsesforhold
        } catch (cause: Throwable) {
            ansettelsesforholdOppslagCounter.labels("failure").inc()
            throw cause
        }
    }
}