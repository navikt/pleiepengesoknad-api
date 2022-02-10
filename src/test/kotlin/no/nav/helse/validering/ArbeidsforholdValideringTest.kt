package no.nav.helse.validering

import no.nav.helse.soknad.*
import no.nav.helse.soknad.validering.valider
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidsforholdValideringTest {

    val arbeidsforhold = Arbeidsforhold(
        jobberNormaltTimer = 33.0,
        arbeidIPeriode = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.NEI
        )
    )

    @Test
    fun `Ved jobberIPerioden=JA og erLiktHverUke er null skal den feile dersom enkeltdager er null`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberProsent = 50.0,
            erLiktHverUke = null,
            enkeltdager = null,
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(arbeidIPeriode = arbeidIPerioden)

        val forventetFeil = "Dersom erLiktHverUke er null, må enkeltDager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=JA og erLiktHverUke er true skal den feile dersom fasteDager er null`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberProsent = 50.0,
            erLiktHverUke = true,
            enkeltdager = null,
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(arbeidIPeriode = arbeidIPerioden)

        val forventetFeil = "Dersom erLiktHverUke er true, kan ikke fasteDager være null."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=JA og erLiktHverUke er false skal den feile dersom enkeltdager er null`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberProsent = 50.0,
            erLiktHverUke = false,
            enkeltdager = null,
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(arbeidIPeriode = arbeidIPerioden)

        val forventetFeil = "Dersom erLiktHverUke er false, kan ikke enkeltdager være null."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=NEI skal den feile dersom enkeltdager er satt`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.NEI,
            erLiktHverUke = false,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(2))),
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(arbeidIPeriode = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=NEI så kan ikke enkeltdager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=NEI skal den feile dersom fasteDager er satt`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.NEI,
            erLiktHverUke = false,
            enkeltdager = null,
            fasteDager = PlanUkedager(mandag = Duration.ofHours(3))
        )

        val arbeidsforhold = arbeidsforhold.copy(arbeidIPeriode = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=NEI så kan ikke faste dager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }
}
