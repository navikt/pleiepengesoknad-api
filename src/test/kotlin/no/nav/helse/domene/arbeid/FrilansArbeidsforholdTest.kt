package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.domene.arbeid.*
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FrilansArbeidsforholdTest {

    companion object{
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        private val nullTimer = Duration.ZERO
    }

    // TODO: 13/04/2022 Lage tester og håndtering av tilfeller hvor man starter/slutter i søknadsperioden

    @Test
    fun `Frilans jobber som normalt i hele søknadsperioden`(){
        val søknadsperiode = Periode(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-10"))
        val frilans = Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            erFortsattFrilanser = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    erLiktHverUke = true,
                    timerPerUkeISnitt = 37.5
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(søknadsperiode.fraOgMed, søknadsperiode.tilOgMed)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[søknadsperiode]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[søknadsperiode]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`(){
        val søknadsperiode = Periode(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-10"))
        val frilans = Frilans(
            harInntektSomFrilanser = false
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(søknadsperiode.fraOgMed, søknadsperiode.tilOgMed)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(nullTimer, perioder[søknadsperiode]!!.jobberNormaltTimerPerDag)
        assertEquals(nullTimer, perioder[søknadsperiode]!!.faktiskArbeidTimerPerDag)
    }

}