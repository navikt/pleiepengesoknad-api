package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.k9format.tilDuration
import no.nav.helse.soknad.PlanUkedager
import java.time.DayOfWeek
import java.time.Duration

class NormalArbeidstid (
    val erLiktHverUke: Boolean? = null, //Unngå default false
    val timerPerUkeISnitt: Double? = null,
    val timerFasteDager: PlanUkedager? = null
) {
    companion object{
        const val DAGER_I_EN_UKE = 5
        val NULL_ARBEIDSTIMER = Duration.ZERO
    }

    init {
        requireNotNull(erLiktHverUke) { "erLiktHverUke må være satt." }
        require(timerFasteDager != null || timerPerUkeISnitt != null) { "Et av feltene må settes" }
        require(timerFasteDager == null || timerPerUkeISnitt == null) { "Et av feltene må være null" }
    }

    fun timerPerDag(gjeldeneDag: DayOfWeek? = null): Duration {
        return if(timerPerUkeISnitt != null) {
            (timerPerUkeISnitt/DAGER_I_EN_UKE).tilDuration()
        } else if(timerFasteDager != null && gjeldeneDag != null) {
            timerFasteDager.timerGittUkedag(gjeldeneDag)
        } else NULL_ARBEIDSTIMER
    }

}