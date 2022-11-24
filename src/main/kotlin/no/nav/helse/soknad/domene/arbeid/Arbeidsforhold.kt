package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_IKKE
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_ULIKE_UKER_TIMER
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_VANLIG
import no.nav.helse.utils.ikkeErHelg
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.LocalDate
import java.util.*
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

class Arbeidsforhold(
    val normalarbeidstid: NormalArbeidstid,
    val arbeidIPeriode: ArbeidIPeriode,
) {

    companion object {
        internal fun k9ArbeidstidInfoMedNullTimer(fraOgMed: LocalDate, tilOgMed: LocalDate) = ArbeidstidInfo()
            .medPerioder(
                mapOf(
                    Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                        .medFaktiskArbeidTimerPerDag(NULL_TIMER)
                        .medJobberNormaltTimerPerDag(NULL_TIMER)
                )
            )
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(arbeidIPeriode.valider("$felt.arbeidIPeriode"))
    }

    fun tilK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) = when (arbeidIPeriode.type) {
        ARBEIDER_VANLIG -> arbeiderVanlig(fraOgMed, tilOgMed)
        ARBEIDER_IKKE -> arbeiderIkke(fraOgMed, tilOgMed)
        ARBEIDER_TIMER_I_SNITT_PER_UKE -> arbeiderTimerISnittPerUke(fraOgMed, tilOgMed)
        ARBEIDER_PROSENT_AV_NORMALT -> arbeiderProsentAvNormalt(fraOgMed, tilOgMed)
        ARBEIDER_ULIKE_UKER_TIMER -> arbeidsukerUlikeTimer(fraOgMed, tilOgMed)
    }

    private fun arbeiderVanlig(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        arbeiderVanligMedNormaltimerSomSnitt(fraOgMed, tilOgMed)

    private fun arbeiderVanligMedNormaltimerSomSnitt(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()
        arbeidstidInfo.medPerioder(
            mapOf(
                Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
                    .medFaktiskArbeidTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
            )
        )
        return arbeidstidInfo
    }

    private fun arbeiderIkke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()

        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
            .medFaktiskArbeidTimerPerDag(NULL_TIMER)

        arbeidstidInfo.medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )

        return arbeidstidInfo
    }

    private fun arbeiderProsentAvNormalt(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.prosentAvNormalt) { "For å regne ut arbeid fra prosentAvNormalt må den være satt." }
        val normaltTimerPerdag = normalarbeidstid.timerPerDagFraSnitt()
        val faktiskTimerPerDag = normaltTimerPerdag.toKotlinDuration()
            .div(100)
            .times(arbeidIPeriode.prosentAvNormalt)
            .toJavaDuration()

        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normaltTimerPerdag)
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)

        return ArbeidstidInfo().medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )
    }

    private fun arbeiderTimerISnittPerUke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.timerPerUke) { "For å regne ut arbeid fra timerPerUke må den være satt." }
        val normaltTimerPerDag = normalarbeidstid.timerPerDagFraSnitt()
        val faktiskTimerPerDag = arbeidIPeriode.timerPerUke.dividedBy(DAGER_PER_UKE)

        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normaltTimerPerDag)
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)

        return ArbeidstidInfo().medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )
    }

    internal fun arbeidsukerUlikeTimer(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.arbeidsuker) { "For å regne ut arbeid fra arbeidsuker må den være satt." }
        val arbeidstidInfo = ArbeidstidInfo()

        arbeidIPeriode.arbeidsuker.map { arbeidsuke: ArbeidsUke ->
            requireNotNull(normalarbeidstid.timerPerUkeISnitt) { "normalarbeidstid.timerPerUkeISnitt må være satt." }
            val periodeUtenHelg: SortedSet<LocalDate> = arbeidsuke.periodeUtenHelg()
            val k9Periode = Periode(periodeUtenHelg.first(), periodeUtenHelg.last())
            val normaltArbeidstimerPerDag = normalarbeidstid.timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)

            val ukedager = periodeUtenHelg.size.toLong()
            val faktiskArbeidstimerPerDag = arbeidsuke.timer.dividedBy(ukedager)

            val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normaltArbeidstimerPerDag)
                .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)

            Pair(k9Periode, arbeidstidPeriodeInfo)
        }
            .forEach { (periode, arbeidstidPeriodeInfo) ->
                //Tar høyde for at enkeltdager kan være utenfor fraOgMed/tilOgMed som kan være justert pga start/slutt innenfor søknadsperioden
                if (periode.fraOgMed.erInnenforPerioden(fraOgMed, tilOgMed)) {
                    arbeidstidInfo.leggeTilPeriode(periode, arbeidstidPeriodeInfo)
                }
            }

        return arbeidstidInfo
    }

    private fun ArbeidsUke.periodeUtenHelg() = periode.fraOgMed.datesUntil(periode.tilOgMed.plusDays(1))
        .filter { it.ikkeErHelg() }
        .toList()
        .toSortedSet()

    private fun LocalDate.erInnenforPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        this.isEqual(fraOgMed) || this.isEqual(tilOgMed) || (this.isAfter(fraOgMed) && this.isBefore(tilOgMed))

    override fun equals(other: Any?) = other === this || other is Arbeidsforhold && this.equals(other)
    private fun equals(other: Arbeidsforhold) =
        this.normalarbeidstid == other.normalarbeidstid && this.arbeidIPeriode == other.arbeidIPeriode
}
