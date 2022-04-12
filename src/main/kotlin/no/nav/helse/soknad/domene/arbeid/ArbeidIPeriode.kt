package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.soknad.PlanUkedager
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

    companion object{
        private const val DAGER_PER_UKE = 5
    }
    fun timerPerDagFraFasteDager(ukedag: DayOfWeek): Duration {
        require(fasteDager != null) { "For å regne ut timer per dag fra faste dager må fasteDager være satt." }
        return fasteDager.timerGittUkedag(ukedag)
    }

    fun k9ArbeidstidFraEnkeltdager(): List<Pair<Periode, ArbeidstidPeriodeInfo>> {
        require(enkeltdager != null) { "For å regne ut arbeid fra enkeltdager må enkeltdager være satt." }
        return enkeltdager.map { it.somK9Arbeidstid() }
    }

    fun timerPerDagFraProsentAvNormalt(normaltimer: Duration): Duration {
        require(prosentAvNormalt != null) { "For å regne ut prosent av normalt må prosentAvNormalt være satt." }
        if(normaltimer == Duration.ZERO) return Duration.ZERO
        return normaltimer.multipliedBy(prosentAvNormalt.toLong()).dividedBy(100)
    }

    fun timerPerDagFraTimerPerUke(): Duration {
        require(timerPerUke != null) { "For å regne ut timer per dag fra timerPerUke må timerPerUke være satt." }
        return timerPerUke.dividedBy(DAGER_PER_UKE.toLong())
    }


}
