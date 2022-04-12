package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbeidIPeriodeTest {

    companion object{
        private val åtteTimer = Duration.ofHours(8)
        private val fireTimer = Duration.ofHours(4)
        private val nullTimer = Duration.ZERO
    }

    @Test
    fun `Arbeid oppgitt som faste ukedager gir riktig svar per gitt dag`(){
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
    fun `Arbeid oppgitt som enkeltdager`(){
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
    fun `Arbeid oppgitt som prosent av normalt gir riktig svar`(){
        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            prosentAvNormalt = 50.0
        )
        assertEquals(fireTimer, arbeidIPeriode.timerPerDagFraProsentAvNormalt(åtteTimer))
        assertEquals(nullTimer, arbeidIPeriode.timerPerDagFraProsentAvNormalt(nullTimer))
    }

    @Test
    fun `Ved 20 timer arbeid snitt per uke er timer per dag 4`(){
        val arbeidIPeriode = ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            timerPerUke = Duration.ofHours(20)
        )
        assertEquals(fireTimer, arbeidIPeriode.timerPerDagFraTimerPerUke())
    }
}