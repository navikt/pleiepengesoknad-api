package no.nav.helse.soknad.domene.arbeid

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

internal val NULL_TIMER = Duration.ZERO
internal const val DAGER_PER_UKE = 5L

class ArbeidstidEnkeltdag(
    val dato: LocalDate,
    val arbeidstimer: Arbeidstimer
) {
    internal fun somK9Arbeidstid(): Pair<Periode, ArbeidstidPeriodeInfo> {
        return Pair(
            Periode(dato, dato),
            ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(arbeidstimer.normalTimer)
                .medFaktiskArbeidTimerPerDag(arbeidstimer.faktiskTimer)
        )
    }

    override fun equals(other: Any?) = other === this || other is ArbeidstidEnkeltdag && this.equals(other)
    private fun equals(other: ArbeidstidEnkeltdag) = this.dato == other.dato && this.arbeidstimer == other.arbeidstimer
}

class Arbeidstimer(
    val normalTimer: Duration,
    val faktiskTimer: Duration
){
    override fun equals(other: Any?) = other === this || other is Arbeidstimer && this.equals(other)
    private fun equals(other: Arbeidstimer) = this.normalTimer == other.normalTimer && this.faktiskTimer == other.faktiskTimer
}

enum class ArbeiderIPeriodenSvar {
    SOM_VANLIG,
    REDUSERT,
    HELT_FRAVÆR
}

enum class ArbeidIPeriodeType {
    ARBEIDER_IKKE,
    ARBEIDER_VANLIG,
    ARBEIDER_ENKELTDAGER,
    ARBEIDER_FASTE_UKEDAGER,
    ARBEIDER_PROSENT_AV_NORMALT,
    ARBEIDER_TIMER_I_SNITT_PER_UKE,
}