package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.Regnskapsfører
import no.nav.helse.soknad.SelvstendigNæringsdrivende
import no.nav.helse.soknad.Virksomhet
import no.nav.helse.soknad.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.helse.soknad.domene.Næringstyper.JORDBRUK_SKOGBRUK
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelvstendigArbeidsforholdTest {

    companion object{
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
    }

    @Test
    fun `Selvstendig næringsdrivende jobber som normalt i hele søknadsperioden`(){
        val selvstendig = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                næringstype = JORDBRUK_SKOGBRUK,
                fiskerErPåBladB = false,
                fraOgMed = LocalDate.parse("2021-02-07"),
                næringsinntekt = 1233123,
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = false,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                harFlereAktiveVirksomheter = false
            ),
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                    arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                )
            )
        )

        val k9ArbeidstidInfo = selvstendig.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo!!.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende uten arbeidsforhold, forventer null`(){
        val selvstendig = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = false
        )
        assertNull(selvstendig.k9ArbeidstidInfo(mandag, fredag))
    }
}
