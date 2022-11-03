package no.nav.helse.domene.arbeid

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.soknad.Periode
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.ArbeidsUke
import no.nav.helse.soknad.domene.arbeid.ArbeidstidEnkeltdag
import no.nav.helse.soknad.domene.arbeid.Arbeidstimer
import no.nav.helse.soknad.domene.arbeid.NULL_TIMER
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidIPeriodeTest {

    companion object {
        private val åtteTimer = Duration.ofHours(8)
        private val fireTimer = Duration.ofHours(4)

        private val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_ENKELTDAGER og fasteDager er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            enkeltdager = null
        ).valider("test", normalArbeidstid).verifiserFeil(1)
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_FASTE_UKEDAGER og fasteDager er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_FASTE_UKEDAGER,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            fasteDager = null
        ).valider("test", normalArbeidstid).verifiserFeil(1)
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_PROSENT_AV_NORMALT og prosentAvNormalt er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            prosentAvNormalt = null
        ).valider("test", normalArbeidstid).verifiserFeil(1)
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_TIMER_I_SNITT_PER_UKE og timerPerUke er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            timerPerUke = null
        ).valider("test", normalArbeidstid).verifiserFeil(1)
    }

    @Test
    fun `Arbeid oppgitt som faste ukedager gir riktig svar per gitt dag`() {
        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_FASTE_UKEDAGER,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            fasteDager = PlanUkedager(
                mandag = åtteTimer,
                tirsdag = fireTimer,
                onsdag = null
            )
        )
        assertEquals(åtteTimer, arbeidIPeriode.timerPerDagFraFasteDager(DayOfWeek.MONDAY))
        assertEquals(fireTimer, arbeidIPeriode.timerPerDagFraFasteDager(DayOfWeek.TUESDAY))
        assertEquals(Duration.ZERO, arbeidIPeriode.timerPerDagFraFasteDager(DayOfWeek.WEDNESDAY))
    }

    @Test
    fun `Arbeid oppgitt som enkeltdager`() {
        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_ENKELTDAGER,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            enkeltdager = listOf(
                ArbeidstidEnkeltdag(
                    dato = LocalDate.parse("2022-01-03"),
                    arbeidstimer = Arbeidstimer(
                        normalTimer = åtteTimer,
                        faktiskTimer = fireTimer
                    )
                )
            )
        )
        val k9Arbeidstid = arbeidIPeriode.k9ArbeidstidFraEnkeltdager()
        assertEquals(1, k9Arbeidstid.size)
        assertEquals(åtteTimer, k9Arbeidstid.first().second.jobberNormaltTimerPerDag)
        assertEquals(fireTimer, k9Arbeidstid.first().second.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Arbeid oppgitt som arbeidsuker`() {
        val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))

        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_ULIKE_UKER_TIMER,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            arbeidsuker = listOf(
                ArbeidsUke(
                    periode = Periode(
                        fraOgMed = LocalDate.parse("2022-01-03"),
                        tilOgMed = LocalDate.parse("2022-01-05")
                    ),
                    prosentAvNormalt = 50.0
                )
            )
        )
        val k9Arbeidstid = arbeidIPeriode.k9ArbeidstidFraArbeidsuker(normalArbeidstid)
        assertEquals(1, k9Arbeidstid.size)
        assertEquals(åtteTimer, k9Arbeidstid.first().second.jobberNormaltTimerPerDag)
        assertEquals(fireTimer, k9Arbeidstid.first().second.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Arbeid oppgitt som prosent av normalt gir riktig svar`() {
        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            prosentAvNormalt = 50.0
        )
        assertEquals(fireTimer, arbeidIPeriode.timerPerDagFraProsentAvNormalt(åtteTimer))
        assertEquals(NULL_TIMER, arbeidIPeriode.timerPerDagFraProsentAvNormalt(NULL_TIMER))
    }

    @Test
    fun `Ved 20 timer arbeid snitt per uke er timer per dag 4`() {
        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            timerPerUke = Duration.ofHours(20)
        )
        assertEquals(fireTimer, arbeidIPeriode.timerPerDagFraTimerPerUke())
    }
}
