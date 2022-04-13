package no.nav.helse.soknad.validering

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.Frilans

fun Frilans.valider(): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    //if(arbeidsforhold != null) feil.addAll(arbeidsforhold.valider("frilans"))

    if(sluttdato != null && sluttdato.isBefore(startdato)){
        feil.add(
            Violation(
                parameterName = "frilans.sluttdato",
                parameterType = ParameterType.ENTITY,
                reason = "Sluttdato kan ikke være før startdato",
                invalidValue = "startdato=$startdato,sluttdato=$sluttdato"
            )
        )
    }

    return feil
}