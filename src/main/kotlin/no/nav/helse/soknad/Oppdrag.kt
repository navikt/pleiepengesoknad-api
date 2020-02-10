package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

internal fun Oppdrag.harGyldigPeriode(): Boolean {
    val now = LocalDate.now();
    if (erPagaende) {
        return now > fraOgMed
    }
    return fraOgMed <= tilOgMed
}

internal fun Oppdrag.erPaagaendeErGyldig(): Boolean {
    if (erPagaende) {
        return tilOgMed == null
    }
    return tilOgMed != null
}

internal fun Oppdrag.harGyldigArbeidsgivernavn(): Boolean{
    return arbeidsgivernavn.isNotBlank() && arbeidsgivernavn.isNotEmpty()
}

internal fun Oppdrag.validate(): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

    if(!harGyldigPeriode()){
        violations.add(
            Violation(
                parameterName = "oppdrag.tilogmed og oppdrag.fraogmed",
                parameterType = ParameterType.ENTITY,
                reason = "Har ikke gyldig periode. Fraogmed kan ikke være nyere enn now",
                invalidValue = tilOgMed
            )
        )
    }

    if(!erPaagaendeErGyldig()){
        violations.add(
            Violation(
                parameterName = "oppdrag.erPagaende",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erPagaende er satt så kan ikke tilogmed være satt",
                invalidValue = erPagaende
            )
        )
    }

    if(!harGyldigArbeidsgivernavn()){
        violations.add(
            Violation(
                parameterName = "oppdrag.arbeidsgivernavn",
                parameterType = ParameterType.ENTITY,
                reason = "Arbeidsgivernavn må være satt ved et oppdrag, kan ikke være blank eller tom",
                invalidValue = arbeidsgivernavn
            )
        )
    }

    return violations
}

data class Oppdrag(
    val arbeidsgivernavn: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
    val erPagaende: Boolean
)