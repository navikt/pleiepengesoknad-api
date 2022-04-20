package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.domene.arbeid.*
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Disabled
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansArbeidsforholdTest {

    companion object{
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        private val nullTimer = Duration.ZERO
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
    }

    // TODO: 13/04/2022 Lage tester og håndtering av tilfeller hvor man starter/slutter i søknadsperioden

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
        assertEquals(nullTimer, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(nullTimer, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    @Disabled
    fun `Frilans som startet og sluttet midt i søknadsperioden`(){ // TODO: 20/04/2022 Må sjekke opp hva som er riktig her mtp K9
        val frilans = Frilans(
            startdato = tirsdag,
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
        assertEquals(3, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            assertEquals(nullTimer, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(nullTimer, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(nullTimer, perioder[Periode(tirsdag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(nullTimer, perioder[Periode(tirsdag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

}