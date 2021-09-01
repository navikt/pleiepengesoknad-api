package no.nav.helse.soknad

import java.time.Duration
import java.time.LocalDate

data class OmsorgstilbudV2(
    val historisk: HistoriskOmsorgstilbud? = null,
    val planlagt: PlanlagtOmsorgstilbud? = null
)

data class HistoriskOmsorgstilbud(
    val enkeltdager: List<OmsorgstilbudEnkeltDag>
)

data class PlanlagtOmsorgstilbud(
    val enkeltdager: List<OmsorgstilbudEnkeltDag>? = null,
    val ukedager: OmsorgstilbudUkedager? = null,
    val vetOmsorgstilbud: VetOmsorgstilbud,
    val erLiktHverDag: Boolean? = null
)

enum class VetOmsorgstilbud {
    VET_ALLE_TIMER,
    VET_NOEN_TIMER,
    VET_IKKE
}

data class OmsorgstilbudEnkeltDag(
    val dato: LocalDate,
    val tid: Duration
)

data class OmsorgstilbudUkedager(
    val mandag: Duration? = null,
    val tirsdag: Duration? = null,
    val onsdag: Duration? = null,
    val torsdag: Duration? = null,
    val fredag: Duration? = null
)
