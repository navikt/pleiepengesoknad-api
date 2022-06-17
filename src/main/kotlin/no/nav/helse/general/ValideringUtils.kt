package no.nav.helse.general

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

internal fun MutableList<String>.krever(resultat: Boolean?, feilmelding: String = "") {
    if (resultat != true) this.add(feilmelding)
}

internal fun MutableList<String>.kreverIkkeNull(verdi: Any?, feilmelding: String = "") {
    if (verdi == null) this.add(feilmelding)
}

internal fun List<String>.somViolation(): Set<Violation> = this.map {
    Violation(
        parameterName = "valideringsfeil",
        parameterType = ParameterType.ENTITY,
        reason = it
    )
}.toSet()