package no.nav.helse.validering

import no.nav.helse.soknad.ArbeidIPeriode
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.Enkeltdag
import no.nav.helse.soknad.JobberIPeriodeSvar
import no.nav.helse.soknad.validering.valider
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidsforholdValideringTest {

    val arbeidsforhold = Arbeidsforhold(
        jobberNormaltTimer = 33.0,
        historiskArbeid = null,
        planlagtArbeid = null
    )

    @Test
    fun `Ved jobberIPerioden=JA og jobberSomVanlig=false skal den feile dersom enkeltdager og fasteDager er null`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberSomVanlig = false,
            erLiktHverUke = false,
            enkeltdager = null,
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(historiskArbeid = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=JA og jobberSomVanlig=false må enkeltdager eller faste dager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=JA og jobberSomVanlig=true skal den feile dersom enkeltdager eller fasteDager er satt`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberSomVanlig = true,
            erLiktHverUke = false,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(2))),
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(historiskArbeid = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=JA og jobberSomVanlig=true så kan ikke enkeltdager eller faste dager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=NEI skal den feile dersom enkeltdager eller fasteDager er satt`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.NEI,
            jobberSomVanlig = true,
            erLiktHverUke = false,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(2))),
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(historiskArbeid = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=NEI/VET_IKKE så kan ikke enkeltdager eller faste dager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }

    @Test
    fun `Ved jobberIPerioden=VET_IKKE skal den feile dersom enkeltdager eller fasteDager er satt`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.VET_IKKE,
            jobberSomVanlig = true,
            erLiktHverUke = false,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(2))),
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(historiskArbeid = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=NEI/VET_IKKE så kan ikke enkeltdager eller faste dager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }
}