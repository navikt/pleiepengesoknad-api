package no.nav.helse.soknad.validering

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.domene.Frilans

fun Frilans.valider(): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    if(harInntektSomFrilanser){
        if(startdato == null){
            feil.add(
                Violation(
                    parameterName = "frilans.startdato",
                    parameterType = ParameterType.ENTITY,
                    reason = "startdato må være satt når man har inntektSomFrilanser.",
                    invalidValue = "startdato=$startdato"
                )
            )
        }

        if(jobberFortsattSomFrilans == null){
            feil.add(
                Violation(
                    parameterName = "frilans.jobberFortsattSomFrilans",
                    parameterType = ParameterType.ENTITY,
                    reason = "jobberFortsattSomFrilans må være satt når man har inntektSomFrilanser.",
                    invalidValue = "jobberFortsattSomFrilans=$jobberFortsattSomFrilans"
                )
            )
        }

    }

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