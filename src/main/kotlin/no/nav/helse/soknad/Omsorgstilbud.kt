package no.nav.helse.soknad

import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.time.Duration
import java.time.LocalDate

data class Omsorgstilbud(
    val erLiktHverUke: Boolean,
    val enkeltdager: List<Enkeltdag>? = null,
    val ukedager: PlanUkedager? = null
)

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration
)

data class PlanUkedager(
    val mandag: Duration? = null,
    val tirsdag: Duration? = null,
    val onsdag: Duration? = null,
    val torsdag: Duration? = null,
    val fredag: Duration? = null
) {
    companion object{
        private val NULL_ARBEIDSTIMER = Duration.ZERO
    }

    internal fun timerGittUkedag(ukedag: DayOfWeek): Duration {
        return when(ukedag){
            MONDAY -> mandag ?: NULL_ARBEIDSTIMER
            TUESDAY -> tirsdag ?: NULL_ARBEIDSTIMER
            WEDNESDAY -> onsdag ?: NULL_ARBEIDSTIMER
            THURSDAY -> torsdag ?: NULL_ARBEIDSTIMER
            FRIDAY -> fredag ?: NULL_ARBEIDSTIMER
            SATURDAY, SUNDAY -> NULL_ARBEIDSTIMER
        }
    }
}