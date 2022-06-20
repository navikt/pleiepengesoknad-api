package no.nav.helse.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.Frilans
import no.nav.helse.soknad.domene.arbeid.*
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansTest {

    companion object{
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
        val arbeidsforholdMedNormaltidSomSnittPerUke = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
            )
        )
        val arbeidsforholdMedNormaltidSomFasteUkedager = Arbeidsforhold(
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
    }

    @Test
    fun `Frilans med valideringsfeil i arbeidsforhold`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    erLiktHverUke = null,
                    timerPerUkeISnitt = null
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_FASTE_UKEDAGER,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG,
                    fasteDager = null
                )
            )
        ).valider("test").verifiserFeil(3)
    }

    @Test
    fun `Frilans hvor sluttdato er før startdato skal gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        ).valider("test").verifiserFeil(1)
    }

    @Test
    fun `Frilans hvor sluttdato og startdato er lik skal ikke gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = LocalDate.parse("2020-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        ).valider("test").verifiserIngenFeil()
    }

    @Test
    fun `Frilans hvor sluttdato er etter startdato skal ikke gi valideringsfeil`(){
        Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = LocalDate.parse("2021-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        ).valider("test").verifiserIngenFeil()
    }

    @Test
    fun `Frilans hvor harInntektSomFrilanser er true med startdato og jobberFortsattSomFrilans som null gir feil`(){
        Frilans(
            startdato = null,
            sluttdato = LocalDate.parse("2029-01-01"),
            jobberFortsattSomFrilans = null,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        ).valider("test").verifiserFeil(2)
    }

    @Test
    fun `Frilans jobber som vanlig i hele søknadsperioden`(){
        val frilans = Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
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
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
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
    fun `Frilans som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = mandag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
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
    fun `Frilans som sluttet siste dag i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet etter søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, torsdag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som faste ukedager`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = onsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomFasteUkedager
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(4, perioder.size)

        listOf(mandag, tirsdag, onsdag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(NULL_TIMER, perioder[Periode(torsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(torsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet første dag i søknadsperioden med normaltid oppgitt som faste ukedager`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = mandag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomFasteUkedager
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
    fun `Frilans som sluttet siste dag i søknadsperioden med normaltid oppgitt som faste ukedager`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomFasteUkedager
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(5, perioder.size)

        listOf(mandag, tirsdag, onsdag, torsdag, fredag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Frilans som sluttet etter søknadsperioden med normaltid oppgitt som faste ukedager`(){
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomFasteUkedager
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

    @Test
    fun `Frilans som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = onsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag )]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som startet etter søknadsperioden startet med normaltid oppgitt som faste ukedager`(){
        val frilans = Frilans(
            startdato = onsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomFasteUkedager
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(4, perioder.size)

        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        listOf(onsdag, torsdag, fredag).forEach{ dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag )]!!.jobberNormaltTimerPerDag)
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Frilans som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`(){
        val frilans = Frilans(
            startdato = tirsdag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(3, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som startet og sluttet i søknadsperioden med normaltid oppgitt som faste ukedager`(){
        val frilans = Frilans(
            startdato = tirsdag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomFasteUkedager
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(5, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
        listOf(tirsdag, onsdag, torsdag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }
}