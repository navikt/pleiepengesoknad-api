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
    fun `Ved jobberIPerioden=JA og erLiktHverUke=false skal den feile dersom enkeltdager er null`(){
        val arbeidIPerioden = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberProsent = 50.0,
            erLiktHverUke = false,
            enkeltdager = null,
            fasteDager = null
        )

        val arbeidsforhold = arbeidsforhold.copy(historiskArbeid = arbeidIPerioden)

        val forventetFeil = "Dersom erLiktHverUke er true, kan ikke enkeltdager være null."
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

        val arbeidsforhold = arbeidsforhold.copy(historiskArbeid = arbeidIPerioden)

        val forventetFeil = "Dersom jobberIPerioden=NEI så kan ikke enkeltdager være satt."
        val feil = arbeidsforhold.valider("frilans").first().reason

        assertEquals(forventetFeil, feil)
    }
}
