package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer


data class ArbeidsforholdAnsatt(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val arbeidsforhold: Arbeidsforhold
)

data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val jobberSomVanlig: Boolean? = null,
    val enkeltdager: List<Enkeltdag>? = null,
    val fasteDager: PlanUkedager? = null
)

enum class JobberIPeriodeSvar {
    JA,
    NEI,
    VET_IKKE
}

enum class Arbeidsform {
    FAST,
    TURNUS,
    VARIERENDE
}

internal fun List<ArbeidsforholdAnsatt>.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    this.mapIndexed { index, arbeidsforholdAnsatt ->
        if (!arbeidsforholdAnsatt.organisasjonsnummer.erGyldigOrganisasjonsnummer()) {
            violations.add(
                Violation(
                    parameterName = "ansatt.arbeidsforholdAnsatt[$index].organisasjonsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig organisasjonsnummer.",
                    invalidValue = arbeidsforholdAnsatt.organisasjonsnummer
                )
            )
        }

        if (arbeidsforholdAnsatt.navn != null && arbeidsforholdAnsatt.navn.erBlankEllerLengreEnn(100)) {
            violations.add(
                Violation(
                    parameterName = "ansatt.arbeidsforholdAnsatt[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navnet på organisasjonen kan ikke være tomt, og kan maks være 100 tegn.",
                    invalidValue = arbeidsforholdAnsatt.navn
                )
            )
        }
    }

    // TODO: 27/09/2021 - Sjekk hva mer som må valideres
    return violations
}

internal fun String.erBlankEllerLengreEnn(maxLength: Int): Boolean = isBlank() || length > maxLength