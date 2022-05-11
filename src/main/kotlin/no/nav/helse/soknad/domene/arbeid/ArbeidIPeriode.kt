package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.general.kreverIkkeNull
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.*
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.DayOfWeek
import java.time.Duration

class ArbeidIPeriode(
    val type: ArbeidIPeriodeType,
    val arbeiderIPerioden: ArbeiderIPeriodenSvar,
    val erLiktHverUke: Boolean? = null,
    val fasteDager: PlanUkedager? = null,
    val prosentAvNormalt: Double? = null,
    val timerPerUke: Duration? = null,
    val enkeltdager: List<ArbeidstidEnkeltdag>? = null
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        when(type){
            ARBEIDER_ENKELTDAGER -> kreverIkkeNull(enkeltdager, "$felt.enkeltdager må være satt dersom type=ARBEIDER_ENKELTDAGER")
            ARBEIDER_FASTE_UKEDAGER -> kreverIkkeNull(fasteDager, "$felt.fasteDager må være satt dersom type=ARBEIDER_FASTE_UKEDAGER")
            ARBEIDER_PROSENT_AV_NORMALT -> kreverIkkeNull(prosentAvNormalt, "$felt.prosentAvNormalt må være satt dersom type=ARBEIDER_PROSENT_AV_NORMALT")
            ARBEIDER_TIMER_I_SNITT_PER_UKE -> kreverIkkeNull(timerPerUke, "$felt.timerPerUke må være satt dersom type=ARBEIDER_TIMER_I_SNITT_PER_UKE")
        }
    }

    internal fun timerPerDagFraFasteDager(ukedag: DayOfWeek): Duration {
        requireNotNull(fasteDager) { "For å regne ut timer per dag fra faste dager må fasteDager være satt." }
        return fasteDager.timerGittUkedag(ukedag)
    }

    internal fun k9ArbeidstidFraEnkeltdager(): List<Pair<Periode, ArbeidstidPeriodeInfo>> {
        requireNotNull(enkeltdager) { "For å regne ut arbeid fra enkeltdager må enkeltdager være satt." }
        return enkeltdager.map { it.somK9Arbeidstid() }
    }

    internal fun timerPerDagFraProsentAvNormalt(normaltimer: Duration): Duration {
        requireNotNull(prosentAvNormalt) { "For å regne ut prosent av normalt må prosentAvNormalt være satt." }
        if(normaltimer == Duration.ZERO) return Duration.ZERO
        return normaltimer.multipliedBy(prosentAvNormalt.toLong()).dividedBy(100)
    }

    internal fun timerPerDagFraTimerPerUke(): Duration {
        requireNotNull(timerPerUke) { "For å regne ut timer per dag fra timerPerUke må timerPerUke være satt." }
        return timerPerUke.dividedBy(DAGER_PER_UKE)
    }

    override fun equals(other: Any?) = this === other || other is ArbeidIPeriode && this.equals(other)
    private fun equals(other: ArbeidIPeriode) = this.type == other.type
            && this.arbeiderIPerioden == other.arbeiderIPerioden
            && this.erLiktHverUke == other.erLiktHverUke
            && this.fasteDager == other.fasteDager
            && this.prosentAvNormalt == other.prosentAvNormalt
            && this.timerPerUke == other.timerPerUke
            && this.enkeltdager == other.enkeltdager

}