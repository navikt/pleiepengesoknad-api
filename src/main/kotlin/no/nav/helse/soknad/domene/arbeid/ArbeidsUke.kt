package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.soknad.Periode
import no.nav.helse.utils.ikkeErHelg
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class ArbeidsUke(
    val periode: Periode,
    val timer: Duration,
    val prosentAvNormalt: Double
) {
    internal fun somK9Arbeidstid(): Pair<K9Periode, ArbeidstidPeriodeInfo> {
        val normaltArbeidstimerPerDag = timer.dividedBy(DAGER_PER_UKE)
        val faktiskArbeidstimerPerDag = normaltArbeidstimerPerDag.multipliedBy(prosentAvNormalt.toLong()).dividedBy(100)
        val periodeUtenHelg = periodeUtenHelg()

        return Pair(
            K9Periode(periodeUtenHelg.first(), periodeUtenHelg.last()),
            ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normaltArbeidstimerPerDag)
                .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)
        )
    }

    private fun periodeUtenHelg() = periode.fraOgMed.datesUntil(periode.tilOgMed)
        .filter { it.ikkeErHelg() }
        .toList()
        .toSortedSet()
}
