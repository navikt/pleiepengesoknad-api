package no.nav.helse.soknad

import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.time.Duration
import java.time.LocalDate

data class Omsorgstilbud(
    val svar: OmsorgstilbudSvar? = null, //TODO 17/08/2022 - Fjerne nullable etter frontend er prodsatt
    val erLiktHverUke: Boolean? = null,
    val enkeltdager: List<Enkeltdag>? = null,
    val ukedager: PlanUkedager? = null
)

enum class OmsorgstilbudSvar {
    FAST_OG_REGELMESSIG, DELVIS_FAST_OG_REGELMESSIG, IKKE_FAST_OG_REGELMESSIG, IKKE_OMSORGSTILBUD
}

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