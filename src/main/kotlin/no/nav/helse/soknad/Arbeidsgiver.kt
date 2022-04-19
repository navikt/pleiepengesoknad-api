package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null
) {
    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return if(arbeidsforhold != null) {
            arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
        } else {
            ArbeidstidInfo()
                .medPerioder(
                    mapOf(
                        Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                            .medFaktiskArbeidTimerPerDag(Duration.ZERO)
                            .medJobberNormaltTimerPerDag(Duration.ZERO)
                    )
                )
        }
    }
}

internal fun List<Arbeidsgiver>.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    this.mapIndexed { index, arbeidsgiver ->
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