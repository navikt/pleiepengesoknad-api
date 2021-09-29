package no.nav.helse.soknad

import java.time.Duration
import java.time.LocalDate

data class Omsorgstilbud(
    val historisk: HistoriskOmsorgstilbud? = null,
    val planlagt: PlanlagtOmsorgstilbud? = null
)

data class HistoriskOmsorgstilbud(
    val enkeltdager: List<Enkeltdag>
)

data class PlanlagtOmsorgstilbud(
    val enkeltdager: List<Enkeltdag>? = null,
    val ukedager: PlanUkedager? = null,
    val vetOmsorgstilbud: VetOmsorgstilbud,
    val erLiktHverDag: Boolean? = null
)

enum class VetOmsorgstilbud {
    VET_ALLE_TIMER,
    VET_IKKE
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
)
