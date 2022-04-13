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
    }

    init {
        requireNotNull(erLiktHverUke) { "erLiktHverUke må være satt." }
        require(timerFasteDager != null || timerPerUkeISnitt != null) { "Et av feltene må settes" }
        require(timerFasteDager == null || timerPerUkeISnitt == null) { "Et av feltene må være null" }
    }

    internal fun harOppgittTimerSomSnitt() = timerPerUkeISnitt != null
    internal fun harOppgittTimerSomFasteDager() = timerFasteDager != null

    internal fun timerPerDagFraSnitt(): Duration {
        requireNotNull(timerPerUkeISnitt) { "timerPerUkeISnitt må være satt for å hente timer per dag fra snitt." }
        return (timerPerUkeISnitt/DAGER_I_EN_UKE).tilDuration()
    }

    internal fun timerPerDagFraFasteDager(gjeldeneUkedag: DayOfWeek): Duration {
        requireNotNull(timerFasteDager) { "timerFasteDager må være satt for å hente timer per dag fra faste dager." }
        return timerFasteDager.timerGittUkedag(gjeldeneUkedag)
    }

    override fun equals(other: Any?) = this === other || other is NormalArbeidstid && this.equals(other)
    private fun equals(other: NormalArbeidstid) = this.erLiktHverUke == other.erLiktHverUke
            && this.timerPerUkeISnitt == other.timerPerUkeISnitt
            && this.timerFasteDager == other.timerFasteDager
}