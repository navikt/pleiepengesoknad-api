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
    internal fun `gitt en full uke inkludert helg, forvent dager uten helg er mappet`() {
        val (periode: K9Periode, arbeidstidPeriodeInfo) = ArbeidsUke(
            periode = Periode(
                fraOgMed = LocalDate.parse("2022-10-17"),
                tilOgMed = LocalDate.parse("2022-10-23")
            ),
            timer = Duration.ofHours(40),
            prosentAvNormalt = 50.0
        ).somK9Arbeidstid()

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
        val (periode: K9Periode, arbeidstidPeriodeInfo) = ArbeidsUke(
            periode = Periode(
                fraOgMed = LocalDate.parse("2022-10-10"),
                tilOgMed = LocalDate.parse("2022-10-10")
            ),
            timer = Duration.ofHours(40),
            prosentAvNormalt = 50.0
        ).somK9Arbeidstid()

        val forventetPeriode = K9Periode(LocalDate.parse("2022-10-10"), LocalDate.parse("2022-10-10"))
        val forventetArbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
            .medFaktiskArbeidTimerPerDag(Duration.ofHours(4))

        assertEquals(forventetPeriode, periode)
        assertEquals(forventetArbeidstidPeriodeInfo.jobberNormaltTimerPerDag, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
        assertEquals(forventetArbeidstidPeriodeInfo.faktiskArbeidTimerPerDag, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
    }
}
