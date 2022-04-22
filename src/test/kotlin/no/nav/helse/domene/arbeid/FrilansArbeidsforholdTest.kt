package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.*
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansArbeidsforholdTest {

    companion object{
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
    }

    @Test
    fun `Frilans jobber som normalt i hele søknadsperioden`(){
        val frilans = Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    erLiktHverUke = true,
                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`(){
        val frilans = Frilans(
            harInntektSomFrilanser = false
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`(){ // TODO: 20/04/2022 Må sjekke opp hva som er riktig her mtp K9
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    erLiktHverUke = true,
                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`(){ // TODO: 20/04/2022 Må sjekke opp hva som er riktig her mtp K9
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = mandag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    erLiktHverUke = true,
                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som faste ukedager`(){ // TODO: 20/04/2022 Må sjekke opp hva som er riktig her mtp K9
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    erLiktHverUke = true,
                    timerFasteDager = PlanUkedager(
                        mandag = syvOgEnHalvTime,
                        tirsdag = syvOgEnHalvTime,
                        onsdag = syvOgEnHalvTime,
                        torsdag = syvOgEnHalvTime,
                        fredag = syvOgEnHalvTime
                    )
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(5, perioder.size)

        listOf(mandag, tirsdag, onsdag, torsdag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

}