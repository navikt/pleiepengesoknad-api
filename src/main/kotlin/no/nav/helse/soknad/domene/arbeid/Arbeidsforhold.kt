package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.k9format.ukedagerTilOgMed
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.LocalDate

class Arbeidsforhold(
    val normalarbeidstid: NormalArbeidstid,
    val arbeidIPeriode: ArbeidIPeriode
) {
    fun tilK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate, sluttetIPerioden: LocalDate? = null) = when(arbeidIPeriode.type){
        ArbeidIPeriodeType.ARBEIDER_VANLIG -> arbeiderVanlig(fraOgMed, tilOgMed, sluttetIPerioden)
        ArbeidIPeriodeType.ARBEIDER_IKKE -> arbeiderIkke(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER -> arbeiderEnkeltdager()
        ArbeidIPeriodeType.ARBEIDER_FASTE_UKEDAGER -> arbeiderFasteUkedager(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT -> arbeiderProsentAvNormalt(fraOgMed, tilOgMed)
        ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE -> arbeiderTimerISnittPerUke(fraOgMed, tilOgMed)
    }

    private fun arbeiderVanlig(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        sluttetIPerioden: LocalDate?
    ) = when {
        normalarbeidstid.harOppgittTimerSomSnitt() -> arbeiderVanligMedNormaltimerSomSnitt(fraOgMed, tilOgMed, sluttetIPerioden)
        normalarbeidstid.harOppgittTimerSomFasteDager() -> arbeiderVanligMedNormaltimerSomFasteDager(fraOgMed, tilOgMed, sluttetIPerioden)
        else -> throw Exception("Klarte ikke mappe opp ARBEIDER_VANLIG fordi normalarbeidstid har oppgitt verken snitt eller fastedager")
    }

    private fun arbeiderVanligMedNormaltimerSomFasteDager(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        sluttetIPerioden: LocalDate?
    ): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()
        fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { ukedagIPerioden ->
            if (sluttetIPerioden != null && sluttetIPerioden.isBefore(ukedagIPerioden)) {
                arbeidstidInfo.leggeTilPeriode(
                    Periode(ukedagIPerioden, ukedagIPerioden),
                    arbeidstidPeriodeInfoMedNullTimer()
                )
            } else {
                arbeidstidInfo.leggeTilPeriode(
                    Periode(ukedagIPerioden, ukedagIPerioden),
                    ArbeidstidPeriodeInfo()
                        .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraFasteDager(ukedagIPerioden.dayOfWeek))
                        .medFaktiskArbeidTimerPerDag(normalarbeidstid.timerPerDagFraFasteDager(ukedagIPerioden.dayOfWeek))
                )
            }
        }
        return arbeidstidInfo
    }

    private fun arbeiderVanligMedNormaltimerSomSnitt(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        sluttetIPerioden: LocalDate?
    ): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()
        val normalTimer = normalarbeidstid.timerPerDagFraSnitt()
        if(sluttetIPerioden != null && sluttetIPerioden.isBefore(tilOgMed)){
            arbeidstidInfo.leggeTilPeriode(
                Periode(fraOgMed, sluttetIPerioden),
                ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalTimer)
                    .medFaktiskArbeidTimerPerDag(normalTimer)
            )

            arbeidstidInfo.leggeTilPeriode(
                Periode(sluttetIPerioden.plusDays(1), tilOgMed), arbeidstidPeriodeInfoMedNullTimer()
            )
        } else {
            arbeidstidInfo.medPerioder(
                mapOf(Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalTimer)
                    .medFaktiskArbeidTimerPerDag(normalTimer))
            )
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

    private fun arbeidstidPeriodeInfoMedNullTimer() = ArbeidstidPeriodeInfo()
        .medJobberNormaltTimerPerDag(NULL_TIMER)
        .medFaktiskArbeidTimerPerDag(NULL_TIMER)

    override fun equals(other: Any?) = other === this || other is Arbeidsforhold && this.equals(other)
    private fun equals(other: Arbeidsforhold) = this.normalarbeidstid == other.normalarbeidstid && this.arbeidIPeriode == other.arbeidIPeriode
}