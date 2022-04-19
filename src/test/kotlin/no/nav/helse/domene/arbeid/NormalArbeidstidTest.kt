package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NormalArbeidstidTest {

    @Test
    fun `Gir feil dersom verken timerPerUkeISnitt eller timerFasteDager er satt`(){
        assertThrows<IllegalArgumentException> {
            NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = null,
                timerFasteDager = null
            )
        }
    }

    @Test
    fun `Gir feil dersom timerPerUkeISnitt og timerFasteDager er satt`(){
        assertThrows<IllegalArgumentException> {
            NormalArbeidstid(
                erLiktHverUke = true,
                timerPerUkeISnitt = 37.5,
                timerFasteDager = PlanUkedager()
            )
        }
    }

    @Test
    fun `Gir feil dersom erLiktHverUke er null`(){
        assertThrows<IllegalArgumentException> {
            NormalArbeidstid(
                erLiktHverUke = null,
                timerPerUkeISnitt = 37.5,
                timerFasteDager = null
            )
        }
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerPerUkeISnitt - 37,5 timer per uke gir 7,5 per dag`(){
        val normalarbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = 37.5,
            timerFasteDager = null
        )

        assertTrue(normalarbeidstid.harOppgittTimerSomSnitt())
        assertFalse(normalarbeidstid.harOppgittTimerSomFasteDager())
        assertEquals(Duration.ofHours(7).plusMinutes(30), normalarbeidstid.timerPerDagFraSnitt())
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerFasteDager`(){
        val femTimer = Duration.ofHours(5).plusMinutes(45)
        val normalarbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = null,
            timerFasteDager = PlanUkedager(mandag = femTimer, tirsdag = femTimer)
        )
        assertTrue(normalarbeidstid.harOppgittTimerSomFasteDager())
        assertFalse(normalarbeidstid.harOppgittTimerSomSnitt())

        assertEquals(femTimer, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.MONDAY))
        assertEquals(femTimer, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.TUESDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.WEDNESDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.THURSDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.FRIDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.SATURDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDagFraFasteDager(DayOfWeek.SUNDAY))
    }

}