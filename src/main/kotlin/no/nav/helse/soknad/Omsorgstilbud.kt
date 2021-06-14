package no.nav.helse.soknad

import java.time.Duration
import java.time.LocalDate

data class Omsorgstilbud(
    val fasteDager: OmsorgstilbudFasteDager? = null,
    val enkeltDager: List<OmsorgstilbudEnkeltDag>? = null,
    val vetOmsorgstilbud: VetOmsorgstilbud
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

data class OmsorgstilbudFasteDager(
    val mandag: Duration? = null,
    val tirsdag: Duration? = null,
    val onsdag: Duration? = null,
    val torsdag: Duration? = null,
    val fredag: Duration? = null
)
