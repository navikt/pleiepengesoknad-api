package no.nav.helse.soknad.validering

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.ArbeidIPeriode
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.JobberIPeriodeSvar

fun Arbeidsforhold.valider(path: String) = mutableSetOf<Violation>().apply {
    addAll(arbeidIPeriode.valider("$path.arbeidsforhold.arbeidIPeriode"))
}

fun ArbeidIPeriode.valider(path: String): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    when (jobberIPerioden) {
        JobberIPeriodeSvar.JA -> {
            if (erLiktHverUke == null && enkeltdager == null) {
                feil.add(
                    Violation(
                        parameterName = "$path.erLiktHverUke && $path.enkeltDager",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom erLiktHverUke er null, må enkeltDager være satt.",
                        invalidValue = "erLiktHverUke=$erLiktHverUke && enkeltDager=$enkeltdager"
                    )
                )
            }

            if (erLiktHverUke == true && fasteDager == null) {
                feil.add(Violation(
                    parameterName = "$path.erLiktHverUke && $path.fasteDager",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom erLiktHverUke er true, kan ikke fasteDager være null.",
                    invalidValue = "erLiktHverUke=$erLiktHverUke && fasteDager=$fasteDager"
                ))
            }

            if (erLiktHverUke == false && enkeltdager == null) {
                feil.add(Violation(
                    parameterName = "$path.erLiktHverUke && $path.enkeltdager",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom erLiktHverUke er false, kan ikke enkeltdager være null.",
                    invalidValue = "erLiktHverUke=$erLiktHverUke && enkeltdager=$enkeltdager"
                ))
            }
        }

        JobberIPeriodeSvar.NEI -> {
            if (enkeltdager != null) {
                feil.add(
                    Violation(
                        parameterName = "$path.jobberIPerioden && $path.enkeltdager",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom jobberIPerioden=NEI så kan ikke enkeltdager være satt.",
                        invalidValue = "jobberIPerioden=$jobberIPerioden && enkeltdager=$enkeltdager"
                    )
                )
            }
            if (fasteDager != null) {
                feil.add(
                    Violation(
                        parameterName = "$path.jobberIPerioden && $path.fasteDager",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom jobberIPerioden=NEI så kan ikke faste dager være satt.",
                        invalidValue = "jobberIPerioden=$jobberIPerioden && fasteDager=$fasteDager"
                    )
                )
            }
            if (erLiktHverUke != null) {
                feil.add(
                    Violation(
                        parameterName = "$path.jobberIPerioden && $path.erLiktHverUke",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom jobberIPerioden=NEI så kan ikke erLiktHverUke være satt.",
                        invalidValue = "jobberIPerioden=$jobberIPerioden && erLiktHverUke=$erLiktHverUke"
                    )
                )
            }
        }
    }

    return feil
}