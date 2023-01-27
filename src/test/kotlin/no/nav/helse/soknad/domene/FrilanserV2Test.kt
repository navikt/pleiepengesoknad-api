package no.nav.helse.soknad.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.helse.soknad.domene.arbeid.NULL_TIMER
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

internal class FrilanserV2Test {
    companion object {
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
    }

    @Test
    fun `Frilans med valideringsfeil i arbeidsforhold`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = null,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            erLiktHverUke = null,
                            timerPerUkeISnitt = null
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
                            arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG,
                            timerPerUke = null
                        )
                    )
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserFeil(3)
    }

    @Test
    fun `Frilans hvor sluttdato er før startdato skal gi valideringsfeil`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = LocalDate.parse("2019-01-01"),
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = null
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserFeil(1)
    }

    @Test
    fun `Frilans hvor sluttdato og startdato er lik skal ikke gi valideringsfeil`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = LocalDate.parse("2020-01-01"),
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = null
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserIngenFeil()
    }


    @Test
    fun `Frilans der harOppdragIPerioden=JA skal gi valideringsfeil dersom ansattTom ikke er null`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = LocalDate.parse("2020-01-01"),
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = null
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserFeil(1)
    }

    @Test
    fun `Frilans der harOppdragIPerioden=JA_MEN_AVSLUTTES_I_PERIODEN skal gi valideringsfeil dersom ansattTom er null`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = null,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = null
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserFeil(1)
    }

    @Test
    fun `Frilans der harOppdragIPerioden=JA_MEN_AVSLUTTES_I_PERIODEN og ansattTom er etter søknadsperiodens sluttdato skal gi valideringsfeil`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = LocalDate.parse("2020-01-15"),
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = null
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserFeil(1)
    }

    @Test
    fun `Frilans hvor sluttdato er etter startdato skal ikke gi valideringsfeil`() {
        FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = LocalDate.parse("2020-01-01"),
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = null
                )
            )
        ).valider("frilanseroppdrag", LocalDate.parse("2020-01-10")).verifiserIngenFeil()
    }

    @Test
    fun `Frilans jobber som vanlig i hele søknadsperioden`() {
        val oppdrag = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    ansattFom = LocalDate.parse("2020-01-01"),
                    ansattTom = null,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        ).oppdrag.first()

        val k9ArbeidstidInfo = oppdrag.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(1, perioder.size)
        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = false,
            oppdrag = listOf()
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(1, perioder.size)
        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = mandag,
                    ansattTom = torsdag,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(2, perioder.size)

        val arbeidstidPeriodeInfo = perioder[Periode(mandag, torsdag)]!!

        kotlin.test.assertEquals(syvOgEnHalvTime, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
        kotlin.test.assertEquals(syvOgEnHalvTime, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)

        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = mandag,
                    ansattTom = mandag,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(2, perioder.size)

        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag
        )

        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans som sluttet siste dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = mandag,
                    ansattTom = fredag,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(1, perioder.size)

        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans som sluttet etter søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    ansattFom = mandag,
                    ansattTom = fredag,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, torsdag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(1, perioder.size)

        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    ansattFom = onsdag,
                    ansattTom = null,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(2, perioder.size)

        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag
        )

        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Frilans som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = tirsdag,
                    ansattTom = torsdag,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(3, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            kotlin.test.assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            kotlin.test.assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        kotlin.test.assertEquals(
            syvOgEnHalvTime,
            perioder[Periode(tirsdag, torsdag)]!!.jobberNormaltTimerPerDag
        )
        kotlin.test.assertEquals(
            syvOgEnHalvTime,
            perioder[Periode(tirsdag, torsdag)]!!.faktiskArbeidTimerPerDag
        )
    }

    @Test
    fun `Fosterhjemgodtgjørelse mappes med 0-0 timer`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.FOSTERFORELDER,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = mandag,
                    ansattTom = fredag,
                    styremedlemHeleInntekt = null,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(1, perioder.size)
        kotlin.test.assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        kotlin.test.assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Omsorgsstønad mappes med 0-0 timer dersom det ikke tapes inntekt`() {
        val frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    organisasjonsnummer = "123",
                    offentligIdent = null,
                    manuellOppføring = false,
                    oppdragType = FrilanserOppdragType.OMSORGSSTØNAD,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                    ansattFom = mandag,
                    ansattTom = fredag,
                    styremedlemHeleInntekt = false,
                    arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
                )
            )
        )

        val k9ArbeidstidInfo = frilanser.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        kotlin.test.assertEquals(1, perioder.size)
        kotlin.test.assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        kotlin.test.assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }
}
