package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.soknad.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import kotlin.test.assertEquals
import no.nav.k9.søknad.felles.type.Periode as K9Periode

internal class ArbeidsUkeTest {

    @Test
    internal fun `gitt en full uke oppgitt i prosent inkludert helg, forvent dager uten helg er mappet`() {
        val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
        val (periode: K9Periode, arbeidstidPeriodeInfo) = ArbeidsUke(
            periode = Periode(
                fraOgMed = LocalDate.parse("2022-10-17"),
                tilOgMed = LocalDate.parse("2022-10-23")
            ),
            prosentAvNormalt = 50.0
        ).somK9Arbeidstid(normalArbeidstid)

        val forventetPeriode = K9Periode(LocalDate.parse("2022-10-17"), LocalDate.parse("2022-10-21"))
        val forventetArbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
            .medFaktiskArbeidTimerPerDag(Duration.ofHours(4))

        assertEquals(forventetPeriode, periode)
        assertEquals(forventetArbeidstidPeriodeInfo.jobberNormaltTimerPerDag, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
        assertEquals(forventetArbeidstidPeriodeInfo.faktiskArbeidTimerPerDag, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
    }

    @Test
    internal fun `gitt en full uke oppgitt i timer inkludert helg, forvent dager uten helg er mappet`() {
        val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
        val (periode: K9Periode, arbeidstidPeriodeInfo) = ArbeidsUke(
            periode = Periode(
                fraOgMed = LocalDate.parse("2022-10-17"),
                tilOgMed = LocalDate.parse("2022-10-23")
            ),
            timer = Duration.ofHours(20)
        ).somK9Arbeidstid(normalArbeidstid)

        val forventetPeriode = K9Periode(LocalDate.parse("2022-10-17"), LocalDate.parse("2022-10-21"))
        val forventetArbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
            .medFaktiskArbeidTimerPerDag(Duration.ofHours(4))

        assertEquals(forventetPeriode, periode)
        assertEquals(forventetArbeidstidPeriodeInfo.jobberNormaltTimerPerDag, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
        assertEquals(forventetArbeidstidPeriodeInfo.faktiskArbeidTimerPerDag, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
    }

    @Test
    internal fun `en arbeidsuke med en arbeidsdag, skal ikke feile`() {
        val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
        val (periode: K9Periode, arbeidstidPeriodeInfo) = ArbeidsUke(
            periode = Periode(
                fraOgMed = LocalDate.parse("2022-10-10"),
                tilOgMed = LocalDate.parse("2022-10-10")
            ),
            timer = Duration.ofHours(4),
        ).somK9Arbeidstid(normalArbeidstid)

        val forventetPeriode = K9Periode(LocalDate.parse("2022-10-10"), LocalDate.parse("2022-10-10"))
        val forventetArbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
            .medFaktiskArbeidTimerPerDag(Duration.ofHours(4))

        assertEquals(forventetPeriode, periode)
        assertEquals(forventetArbeidstidPeriodeInfo.jobberNormaltTimerPerDag, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
        assertEquals(forventetArbeidstidPeriodeInfo.faktiskArbeidTimerPerDag, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
    }
}
