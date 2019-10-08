package no.nav.helse.soker

import java.time.LocalDate
import java.time.ZoneId

private val ZONE_ID = ZoneId.of("Europe/Oslo")
private const val MYNDIG_ALDER = 18L

private fun erMyndig(fodselsdato: LocalDate) : Boolean {
    val myndighetsDato = fodselsdato.plusYears(MYNDIG_ALDER)
    val dagensDato = LocalDate.now(ZONE_ID)
    return myndighetsDato.isBefore(dagensDato) || myndighetsDato.isEqual(dagensDato)
}

data class Soker (
    val aktoerId: String,
    val fodselsdato: LocalDate,
    val fodselsnummer: String,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val myndig : Boolean = erMyndig(fodselsdato)
)

