package no.nav.helse.soknad

import java.time.Duration
import java.time.LocalDate

// TODO: 10/05/2021 Ugår
enum class TilsynsordningSvar {
    ja,
    nei,
    vetIkke
}

// TODO: 10/05/2021 Utgår
enum class TilsynsordningVetIkkeSvar {
    erSporadisk,
    erIkkeLagetEnPlan,
    annet
}

// TODO: 10/05/2021 Utgår
data class TilsynsordningJa(
    val mandag: Duration?,
    val tirsdag: Duration?,
    val onsdag: Duration?,
    val torsdag: Duration?,
    val fredag: Duration?,
    val tilleggsinformasjon: String? = null // TODO: 07/05/2021 utgår
) {
    override fun toString(): String {
        return "TilsynsordningJa(mandag=${mandag}, tirsdag=${tirsdag}, onsdag=${onsdag}, torsdag=${torsdag}, fredag=${fredag})"
    }
}

// TODO: 10/05/2021 Utgår
data class TilsynsordningVetIkke(
    val svar: TilsynsordningVetIkkeSvar,
    val annet: String? = null
) {
    override fun toString(): String {
        return "TilsynsordningVetIkke(svar=${svar})"
    }
}

// TODO: 10/05/2021 Utgår
data class Tilsynsordning(
    val svar: TilsynsordningSvar? = null,
    val ja: TilsynsordningJa? = null,
    val vetIkke: TilsynsordningVetIkke? = null
)

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
