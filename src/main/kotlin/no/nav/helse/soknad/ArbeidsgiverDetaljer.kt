package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer


data class ArbeidsgiverDetaljer(
    val organisasjoner: List<OrganisasjonDetaljer>
)

data class OrganisasjonDetaljer(
    val navn: String? = null,
    val skalJobbe: SkalJobbe,
    val organisasjonsnummer: String,
    val jobberNormaltTimer: Double,
    val skalJobbeProsent: Double,
    val vetIkkeEkstrainfo: String? = null,
    val arbeidsform: Arbeidsform,
)

enum class Arbeidsform{
    FAST,
    TURNUS,
    VARIERENDE
}

internal fun List<OrganisasjonDetaljer>.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, organisasjon ->
        if (!organisasjon.organisasjonsnummer.erGyldigOrganisasjonsnummer()) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].organisasjonsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig organisasjonsnummer.",
                    invalidValue = organisasjon.organisasjonsnummer
                )
            )
        }

        if (organisasjon.navn != null && organisasjon.navn.erBlankEllerLengreEnn(100)) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navnet på organisasjonen kan ikke være tomt, og kan maks være 100 tegn.",
                    invalidValue = organisasjon.navn
                )
            )
        }

        organisasjon.skalJobbeProsent.apply {
            if (this !in 0.0..100.0) {
                violations.add(
                    Violation(
                        parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent",
                        parameterType = ParameterType.ENTITY,
                        reason = "Skal jobbe prosent må være mellom 0 og 100.",
                        invalidValue = this
                    )
                )
            }
        }

        when (organisasjon.skalJobbe) {
            SkalJobbe.JA -> {
                organisasjon.skalJobbeProsent.let {
                    if (it != 100.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ulik 100%. Dersom skalJobbe = 'ja', så må skalJobbeProsent være 100%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            SkalJobbe.NEI -> {
                organisasjon.skalJobbeProsent.let {
                    if (it != 0.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ulik 0%. Dersom skalJobbe = 'nei', så må skalJobbeProsent være 0%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            SkalJobbe.REDUSERT -> {
                organisasjon.skalJobbeProsent.let {
                    if (it !in 1.0..99.9) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent ligger ikke mellom 1% og 99%. Dersom skalJobbe = 'redusert', så må skalJobbeProsent være mellom 1% og 99%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            SkalJobbe.VET_IKKE -> {
                organisasjon.skalJobbeProsent.let {
                    if (it != 0.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ikke 0%. Dersom skalJobbe = 'vet ikke', så må skalJobbeProsent være 0%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
        }
    }
    return violations
}

internal fun String.erBlankEllerLengreEnn(maxLength: Int): Boolean = isBlank() || length > maxLength
