package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer


data class ArbeidsgiverDetaljer(
    val organisasjoner: List<OrganisasjonDetaljer>
)

data class OrganisasjonDetaljer(
    val navn: String? = null,
    val skalJobbe: String,
    val organisasjonsnummer: String,
    val jobberNormaltTimer: Double,
    val skalJobbeProsent: Double,
    val vetIkkeEkstrainfo: String? = null,
    val arbeidsform: Arbeidsform? = null //TODO 09.02.2021 - Fjerne nullable når prodsatt. Påbudt felt
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

        //TODO 09.02.2021 - Settes på når feltet er prodsatt
        /*
        if(organisasjon.arbeidsform == null){
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].arbeidsform",
                    parameterType = ParameterType.ENTITY,
                    reason = "arbeidsform kan ikke være null",
                    invalidValue = organisasjon.arbeidsform
                )
            )
        }*/

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
            "ja" -> {
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
            "nei" -> {
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
            "redusert" -> {
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
            "vetIkke" -> {
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
            else -> violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbe",
                    parameterType = ParameterType.ENTITY,
                    reason = "Skal jobbe har ikke riktig verdi. Gyldige verdier er: ja, nei, redusert, vetIkke",
                    invalidValue = organisasjon.skalJobbe
                )
            )
        }
    }
    return violations
}

internal fun String.erBlankEllerLengreEnn(maxLength: Int): Boolean = isBlank() || length > maxLength