package no.nav.helse.soknad.validering

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.ArbeidIPeriode
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.JobberIPeriodeSvar

fun Arbeidsforhold.valider(path: String): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    if(historiskArbeid != null) feil.addAll(historiskArbeid.valider("$path.arbeidsforhold.historiskArbeid"))
    if(planlagtArbeid != null) feil.addAll(planlagtArbeid.valider("$path.arbeidsforhold.planlagtArbeid"))

    return feil
}

fun ArbeidIPeriode.valider(path: String): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    if(jobberIPerioden()){
        if(jobberSomVanlig == null){
            feil.add(
                Violation(
                    parameterName = "$path.jobberSomVanlig",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom jobberIPerioden=JA kan ikke jobberSomVanlig være null",
                    invalidValue = "jobberIPerioden=$jobberSomVanlig,jobberSomVanlig=$jobberSomVanlig"
                )
            )
        }

        if(!jobberSomVanlig()){
            if(enkeltdager == null && fasteDager == null){
                feil.add(
                    Violation(
                        parameterName = "$path.jobberSomVanlig",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom jobberIPerioden=JA og jobberSomVanlig=false må enkeltdager eller faste dager være satt.",
                        invalidValue = "jobberSomVanlig=$jobberSomVanlig,enkeltdager=$enkeltdager,fasteDager=$fasteDager"
                    )
                )
            }
        }

        if(jobberSomVanlig()){
            if(enkeltdager != null || fasteDager != null){
                feil.add(
                    Violation(
                        parameterName = "$path.jobberSomVanlig",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom jobberIPerioden=JA og jobberSomVanlig=true så kan ikke enkeltdager eller faste dager være satt.",
                        invalidValue = "jobberSomVanlig=$jobberSomVanlig,enkeltdager=$enkeltdager,fasteDager=$fasteDager"
                    )
                )
            }
        }
    }

    if(!jobberIPerioden()){
        if(enkeltdager != null || fasteDager != null){
            feil.add(
                Violation(
                    parameterName = "$path.jobberIPerioden",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom jobberIPerioden=NEI/VET_IKKE så kan ikke enkeltdager eller faste dager være satt.",
                    invalidValue = "jobberIPerioden=$jobberIPerioden,enkeltdager=$enkeltdager,fasteDager=$fasteDager"
                )
            )
        }
    }

    return feil
}

fun ArbeidIPeriode.jobberIPerioden() : Boolean = this.jobberIPerioden == JobberIPeriodeSvar.JA
fun ArbeidIPeriode.jobberSomVanlig() : Boolean = this.jobberSomVanlig != null && this.jobberSomVanlig