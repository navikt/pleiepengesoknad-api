package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.general.krever
import no.nav.helse.soknad.Periode
import no.nav.helse.utils.ikkeErHelg
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class ArbeidsUke(
    val periode: Periode,
    val timer: Duration? = null,
    val prosentAvNormalt: Double? = null,
) {
    internal fun valider(felt: String, normalArbeidstid: NormalArbeidstid) = mutableListOf<String>().apply {
        krever(
            timer != null && prosentAvNormalt != null,
            "Både $felt.timer og $felt.prosentAvNormalt kan ikke være satt."
        )
        krever(timer == null && prosentAvNormalt == null, "Enten $felt.timer eller $felt.prosentAvNormalt må være satt")
        krever(
            normalArbeidstid.timerPerUkeISnitt != null && prosentAvNormalt != null,
            "Både normalarbeidstid.timerPerUkeISnitt og $felt.prosentAvNormalt må være satt."
        )
    }

    internal fun somK9Arbeidstid(normalarbeidstid: NormalArbeidstid): Pair<K9Periode, ArbeidstidPeriodeInfo> {
        requireNotNull(normalarbeidstid.timerPerUkeISnitt) { "normalarbeidstid.timerPerUkeISnitt må være satt." }
        val periodeUtenHelg = periodeUtenHelg()
        val k9Periode = K9Periode(periodeUtenHelg.first(), periodeUtenHelg.last())
        val normaltArbeidstimerPerDag = normalarbeidstid.timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)

        return when {
            timer != null -> {
                val faktiskArbeidstimerPerDag = timer.dividedBy(DAGER_PER_UKE)

                Pair(
                    k9Periode,
                    ArbeidstidPeriodeInfo()
                        .medJobberNormaltTimerPerDag(normaltArbeidstimerPerDag)
                        .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)
                )
            }

            prosentAvNormalt != null -> {
                val faktiskArbeidstimerPerDag = normaltArbeidstimerPerDag.toKotlinDuration()
                    .div(100)
                    .times(prosentAvNormalt)
                    .toJavaDuration()

                Pair(
                    k9Periode,
                    ArbeidstidPeriodeInfo()
                        .medJobberNormaltTimerPerDag(normaltArbeidstimerPerDag)
                        .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)
                )
            }

            else -> throw IllegalStateException("Burde ikke havne her.")
        }
    }

    private fun periodeUtenHelg() = periode.fraOgMed.datesUntil(periode.tilOgMed.plusDays(1))
        .filter { it.ikkeErHelg() }
        .toList()
        .toSortedSet()
}
