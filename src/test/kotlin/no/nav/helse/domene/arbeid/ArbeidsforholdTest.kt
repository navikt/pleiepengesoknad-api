package no.nav.helse.domene.arbeid

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.ArbeidsUke
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.helse.soknad.domene.arbeid.ArbeidstidEnkeltdag
import no.nav.helse.soknad.domene.arbeid.Arbeidstimer
import no.nav.helse.soknad.domene.arbeid.NULL_TIMER
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidsforholdTest {

    companion object{
        val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val femTimer = Duration.ofHours(5)
        val halvArbeidsdag = Duration.ofHours(3).plusMinutes(45)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
        val lørdag = fredag.plusDays(1)
        val søndag = lørdag.plusDays(1)
    }

    @Test
    fun `Skal gi valideringsfeil dersom normalarbeidstid er feil`(){
        Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = null,
                timerPerUkeISnitt = null,
                timerFasteDager = null
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
            )
        ).valider("test").verifiserFeil(2)
    }

    @Test
    fun `Skal gi valideringsfeil dersom arbeidIPeriode er feil`(){
        Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(32)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG,
                enkeltdager = null
            )
        ).valider("test").verifiserFeil(1)
    }

    @Test
    fun `Jobber som vanlig med normal arbeidstid oppgitt som snitt per uke`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
            )
        )
        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber som vanlig med normal arbeidstid oppgitt som faste dager`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerFasteDager = PlanUkedager(
                    mandag = null, tirsdag = syvOgEnHalvTime, onsdag = null, torsdag = syvOgEnHalvTime, fredag = null
                )
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
            )
        )
        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(5, perioder.size)

        listOf(mandag, onsdag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        listOf(tirsdag, torsdag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Jobber ikke med normal arbeidstid oppgitt som snitt per uke`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber ikke med normal arbeidstid oppgitt som faste dager`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerFasteDager = PlanUkedager(
                    mandag = null, tirsdag = syvOgEnHalvTime, onsdag = null, torsdag = syvOgEnHalvTime, fredag = null
                )
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR
            )
        )
        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(5, perioder.size)

        listOf(mandag, onsdag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        listOf(tirsdag, torsdag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Jobber enkeltdager`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                enkeltdager = listOf(
                    ArbeidstidEnkeltdag(
                        dato = mandag,
                        arbeidstimer = Arbeidstimer(
                            normalTimer = syvOgEnHalvTime,
                            faktiskTimer = femTimer
                        )
                    ),
                    ArbeidstidEnkeltdag(
                        dato = onsdag,
                        arbeidstimer = Arbeidstimer(
                            normalTimer = syvOgEnHalvTime,
                            faktiskTimer = femTimer
                        )
                    ),
                    ArbeidstidEnkeltdag(
                        dato = fredag,
                        arbeidstimer = Arbeidstimer(
                            normalTimer = syvOgEnHalvTime,
                            faktiskTimer = femTimer
                        )
                    )
                )
            )
        )
        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(3, perioder.size)

        listOf(mandag, onsdag, fredag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(femTimer, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Jobber enkeltdager som går utenfor søknadsperioden, forventer at disse ikke blir mappet opp`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                enkeltdager = listOf(
                    ArbeidstidEnkeltdag(
                        dato = mandag,
                        arbeidstimer = Arbeidstimer(
                            normalTimer = syvOgEnHalvTime,
                            faktiskTimer = femTimer
                        )
                    ),
                    ArbeidstidEnkeltdag(
                        dato = onsdag,
                        arbeidstimer = Arbeidstimer(
                            normalTimer = syvOgEnHalvTime,
                            faktiskTimer = femTimer
                        )
                    ),
                    ArbeidstidEnkeltdag(
                        dato = fredag,
                        arbeidstimer = Arbeidstimer(
                            normalTimer = syvOgEnHalvTime,
                            faktiskTimer = femTimer
                        )
                    )
                )
            )
        )
        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(onsdag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(2, perioder.size)

        listOf(onsdag, fredag).forEach { dag ->
            assertEquals(syvOgEnHalvTime, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(femTimer, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Jobber faste ukedager`(){
        val arbeidsforhold = Arbeidsforhold(
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
                type = ArbeidIPeriodeType.ARBEIDER_FASTE_UKEDAGER,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                fasteDager = PlanUkedager(
                    mandag = syvOgEnHalvTime,
                    tirsdag = femTimer,
                    onsdag = null,
                    torsdag = null,
                    fredag = null
                )
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)

        val perioder = k9Arbeid.perioder
        assertEquals(5, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(femTimer, perioder[Periode(tirsdag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber prosent av normalt`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                prosentAvNormalt = 50.0
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime.dividedBy(2), perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber timer i snitt per uke`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                timerPerUke = Duration.ofHours(25)
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(femTimer, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber ulike timer per uke`(){
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                erLiktHverUke = null,
                timerPerUkeISnitt = null,
                timerFasteDager = null
            ), arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_ULIKE_UKER_TIMER,
                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                arbeidsuker = listOf(
                    ArbeidsUke(
                        periode = no.nav.helse.soknad.Periode(mandag, søndag),
                        timer = Duration.ofHours(37).plusMinutes(30),
                        prosentAvNormalt = 50.0
                    )
                )
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(halvArbeidsdag, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }
}
