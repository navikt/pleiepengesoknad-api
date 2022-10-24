package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.general.krever
import no.nav.helse.soknad.PlanUkedager
import java.time.DayOfWeek
import java.time.Duration

class NormalArbeidstid (
    val timerPerUkeISnitt: Duration? = null,
    val timerFasteDager: PlanUkedager? = null
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(timerFasteDager != null || timerPerUkeISnitt != null, "$felt.timerFasteDager eller timerPerUkeISnitt må være satt.")
        krever(timerFasteDager == null || timerPerUkeISnitt == null, "$felt.timerFasteDager eller timerPerUkeISnitt må være null")
    }

    internal fun harOppgittTimerSomSnitt() = timerPerUkeISnitt != null
    internal fun harOppgittTimerSomFasteDager() = timerFasteDager != null

    internal fun timerPerDagFraSnitt(): Duration {
        requireNotNull(timerPerUkeISnitt) { "timerPerUkeISnitt må være satt for å hente timer per dag fra snitt." }
        return (timerPerUkeISnitt.dividedBy(DAGER_PER_UKE))
    }

    internal fun timerPerDagFraFasteDager(gjeldeneUkedag: DayOfWeek): Duration {
        requireNotNull(timerFasteDager) { "timerFasteDager må være satt for å hente timer per dag fra faste dager." }
        return timerFasteDager.timerGittUkedag(gjeldeneUkedag)
    }

    override fun equals(other: Any?) = this === other || other is NormalArbeidstid && this.equals(other)
    private fun equals(other: NormalArbeidstid) = this.timerPerUkeISnitt == other.timerPerUkeISnitt
            && this.timerFasteDager == other.timerFasteDager
}
