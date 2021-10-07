package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.helse.soknad.validering.valider


data class ArbeidsforholdAnsatt(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val arbeidsforhold: Arbeidsforhold? = null
)

data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val jobberSomVanlig: Boolean? = null,
    val erLiktHverUke: Boolean? = null,
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
        arbeidsforholdAnsatt.arbeidsforhold?.let {
            violations.addAll(it.valider("arbeidsgiver[$index]"))
        }

        if (!arbeidsforholdAnsatt.organisasjonsnummer.erGyldigOrganisasjonsnummer()) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.arbeidsforholdAnsatt[$index].organisasjonsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig organisasjonsnummer.",
                    invalidValue = arbeidsforholdAnsatt.organisasjonsnummer
                )
            )
        }

        if (arbeidsforholdAnsatt.navn != null && arbeidsforholdAnsatt.navn.erBlankEllerLengreEnn(100)) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.arbeidsforholdAnsatt[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navnet på organisasjonen kan ikke være tomt, og kan maks være 100 tegn.",
                    invalidValue = arbeidsforholdAnsatt.navn
                )
            )
        }
    }

    return violations
}

internal fun String.erBlankEllerLengreEnn(maxLength: Int): Boolean = isBlank() || length > maxLength