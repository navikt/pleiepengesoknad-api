package no.nav.helse.general

import no.nav.helse.general.auth.Fodselsnummer
import java.time.LocalDate

fun extractFodselsdato(fnr: Fodselsnummer) : LocalDate {
    if (fnr.value.matches(regex = Regex("""\\d{11}"""))) throw IllegalStateException("Ugyldig fødselsnummer '${fnr.value}'")

    val dag = fnr.value.substring(0,2).toInt()
    val maned = fnr.value.substring(2,4).toInt()
    var aar = 1900 + fnr.value.substring(4,6).toInt()

    val individsifre = fnr.value.substring(6,9).toInt()
    if (individsifre >= 500) {
        aar += 100
    }

    return LocalDate.of(aar, maned, dag)
}