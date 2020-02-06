package no.nav.helse.soknad

import java.time.LocalDate

internal fun Oppdrag.harGyldigPeriode(): Boolean {
    val now = LocalDate.now();
    if (erPagaende) {
        return now > fraOgMed
    }
    return fraOgMed < tilOgMed
}

internal fun Oppdrag.kanIkkeVaerePaagaendeOgFerdig(): Boolean {

    if (erPagaende) {
        return tilOgMed == null
    }
    return tilOgMed != null

}

data class Oppdrag(
    val arbeidsgivernavn: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
    val erPagaende: Boolean
)