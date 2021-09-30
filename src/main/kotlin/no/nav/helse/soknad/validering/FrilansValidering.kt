package no.nav.helse.soknad.validering

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.Frilans

fun Frilans.valider(): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    if(arbeidsforhold != null) feil.addAll(arbeidsforhold.valider("frilans"))

    return feil
}