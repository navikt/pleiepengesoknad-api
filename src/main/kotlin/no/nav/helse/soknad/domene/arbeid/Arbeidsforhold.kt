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
        val arbeidstidInfo = ArbeidstidInfo()

        if(normalarbeidstid.harOppgittTimerSomSnitt()){
            val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
                .medFaktiskArbeidTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())

            arbeidstidInfo.medPerioder(
                mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
            )
        } else if(normalarbeidstid.harOppgittTimerSomFasteDager()){
            fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { ukedagIPerioden ->
                val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraFasteDager(ukedagIPerioden.dayOfWeek))
                    .medFaktiskArbeidTimerPerDag(normalarbeidstid.timerPerDagFraFasteDager(ukedagIPerioden.dayOfWeek))

                arbeidstidInfo.leggeTilPeriode(
                    Periode(ukedagIPerioden, ukedagIPerioden),
                    arbeidstidPeriodeInfo
                )
            }
        } else {
            throw Exception("Klarte ikke mappe opp arbeider som vanlig fordi normalarbeidstid har oppgitt verken snitt eller fastedager")
        }

        return arbeidstidInfo
    }

    private fun arbeiderIkke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()

        if(normalarbeidstid.harOppgittTimerSomSnitt()){
            val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
                .medFaktiskArbeidTimerPerDag(NULL_TIMER)

            arbeidstidInfo.medPerioder(
                mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
            )
        } else if(normalarbeidstid.harOppgittTimerSomFasteDager()){
            fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { ukedagIPerioden ->
                val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraFasteDager(ukedagIPerioden.dayOfWeek))
                    .medFaktiskArbeidTimerPerDag(NULL_TIMER)

                arbeidstidInfo.leggeTilPeriode(
                    Periode(ukedagIPerioden, ukedagIPerioden),
                    arbeidstidPeriodeInfo
                )
            }
        } else {
            throw Exception("Klarte ikke mappe opp arbeider ikke fordi normalarbeidstid har oppgitt verken snitt eller fastedager")
        }

        return arbeidstidInfo
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
                .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraFasteDager(dagIPerioden.dayOfWeek))
                .medFaktiskArbeidTimerPerDag(arbeidIPeriode.timerPerDagFraFasteDager(dagIPerioden.dayOfWeek))

            arbeidstidInfo.leggeTilPeriode(
                Periode(dagIPerioden, dagIPerioden), arbeidstidPeriodeInfo
            )
        }

        return arbeidstidInfo
    }

    private fun arbeiderProsentAvNormalt(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val normaltimer = normalarbeidstid.timerPerDagFraSnitt()
        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normaltimer)
            .medFaktiskArbeidTimerPerDag(arbeidIPeriode.timerPerDagFraProsentAvNormalt(normaltimer))

        return ArbeidstidInfo().medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )
    }

    private fun arbeiderTimerISnittPerUke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidPeriodeInfo =
            ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
                .medFaktiskArbeidTimerPerDag(arbeidIPeriode.timerPerDagFraTimerPerUke())

        return ArbeidstidInfo().medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )
    }

    override fun equals(other: Any?) = other === this || other is Arbeidsforhold && this.equals(other)
    private fun equals(other: Arbeidsforhold) = this.normalarbeidstid == other.normalarbeidstid && this.arbeidIPeriode == other.arbeidIPeriode
}