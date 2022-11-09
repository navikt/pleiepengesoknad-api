package no.nav.helse.soknad.domene.arbeid

import java.time.Duration

internal val NULL_TIMER = Duration.ZERO
internal const val DAGER_PER_UKE = 5L

enum class ArbeiderIPeriodenSvar {
    SOM_VANLIG,
    REDUSERT,
    HELT_FRAVÆR
}

enum class ArbeidIPeriodeType {
    ARBEIDER_IKKE,
    ARBEIDER_VANLIG,
    ARBEIDER_PROSENT_AV_NORMALT,
    ARBEIDER_TIMER_I_SNITT_PER_UKE,
    ARBEIDER_ULIKE_UKER_TIMER
}
