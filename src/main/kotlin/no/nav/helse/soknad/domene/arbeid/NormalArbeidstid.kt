package no.nav.helse.soknad.domene.arbeid

import java.time.Duration

class NormalArbeidstid (
    val timerPerUkeISnitt: Duration
) {
    internal fun timerPerDagFraSnitt(): Duration = timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)

    override fun equals(other: Any?) = this === other || other is NormalArbeidstid && this.equals(other)
    private fun equals(other: NormalArbeidstid) = this.timerPerUkeISnitt == other.timerPerUkeISnitt
}
