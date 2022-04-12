package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.k9format.ukedagerTilOgMed
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

class Arbeidsforhold(
    val normalarbeidstid: NormalArbeidstid,
    val arbeidIPeriode: ArbeidIPeriode
) {

    companion object{
        private val NULL_TIMER = Duration.ZERO
    }

    fun tilK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) = when(arbeidIPeriode.type){
        ArbeidIPeriodeType.ARBEIDER_VANLIG -> arbeiderSomVanlig(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_IKKE -> arbeiderIkke(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER -> arbeiderEnkeltdager()
        ArbeidIPeriodeType.ARBEIDER_FASTE_UKEDAGER -> arbeiderFasteUkedager(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT -> arbeiderProsentAvNormalt(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE -> arbeiderTimerISnittPerUke(fraOgMed, tilOgMed)
    }

    private fun arbeiderSomVanlig(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDag())
            .medFaktiskArbeidTimerPerDag(normalarbeidstid.timerPerDag())

        return ArbeidstidInfo()
            .medPerioder(
                mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
            )
    }

    private fun arbeiderIkke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDag())
            .medFaktiskArbeidTimerPerDag(NULL_TIMER)

        return ArbeidstidInfo()
            .medPerioder(
                mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
            )
    }

    private fun arbeiderEnkeltdager(): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()

        arbeidIPeriode.k9ArbeidstidFraEnkeltdager().forEach { (periode, arbeidstidPeriodeInfo) ->
            arbeidstidInfo.leggeTilPeriode(
                periode,
                arbeidstidPeriodeInfo
            )
        }

        return arbeidstidInfo
    }

    private fun arbeiderFasteUkedager(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()

        fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { dagIPerioden ->
            val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDag(dagIPerioden.dayOfWeek))
                .medFaktiskArbeidTimerPerDag(arbeidIPeriode.timerPerDagFraFasteDager(dagIPerioden.dayOfWeek))

            arbeidstidInfo.leggeTilPeriode(
                Periode(dagIPerioden, dagIPerioden), arbeidstidPeriodeInfo
            )
        }

        return arbeidstidInfo
    }

    private fun arbeiderProsentAvNormalt(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()
        fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { ukedagIPerioden ->
            val normaltimer = normalarbeidstid.timerPerDag(ukedagIPerioden.dayOfWeek)
            val faktiskeTimer = arbeidIPeriode.timerPerDagFraProsentAvNormalt(normaltimer)

            arbeidstidInfo.leggeTilPeriode(
                Periode(ukedagIPerioden, ukedagIPerioden),
                ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normaltimer)
                    .medFaktiskArbeidTimerPerDag(faktiskeTimer)
            )
        }

        return arbeidstidInfo
    }

    private fun arbeiderTimerISnittPerUke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()

        fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { ukedagIPerioden ->
            val normaltimer = normalarbeidstid.timerPerDag(ukedagIPerioden.dayOfWeek)
            val faktiskeTimer = arbeidIPeriode.timerPerDagFraTimerPerUke()

            arbeidstidInfo.leggeTilPeriode(
                Periode(ukedagIPerioden, ukedagIPerioden),
                ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normaltimer)
                    .medFaktiskArbeidTimerPerDag(faktiskeTimer)
            )
        }

        return arbeidstidInfo
    }
}