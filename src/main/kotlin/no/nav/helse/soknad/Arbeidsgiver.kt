package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.helse.soknad.validering.valider

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null
)

internal fun List<Arbeidsgiver>.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    this.mapIndexed { index, arbeidsgiver ->
        arbeidsgiver.arbeidsforhold?.let {
            violations.addAll(it.valider("arbeidsgiver[$index]"))
        }

        if (!arbeidsgiver.organisasjonsnummer.erGyldigOrganisasjonsnummer()) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere[$index].organisasjonsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig organisasjonsnummer.",
                    invalidValue = arbeidsgiver.organisasjonsnummer
                )
            )
        }

        if (arbeidsgiver.navn.isNullOrBlank()) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navnet på organisasjonen kan ikke være tomt eller kun whitespace.",
                    invalidValue = arbeidsgiver.navn
                )
            )
        }
    }

    return violations
}