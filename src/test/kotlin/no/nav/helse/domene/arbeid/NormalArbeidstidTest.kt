package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek.*
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
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
                timerFasteDager = PlanUkedager()
            )
        }
    }

    @Test
    fun `Gir feil dersom erLiktHverUke er null`(){
        assertThrows<IllegalArgumentException> {
            NormalArbeidstid(
                erLiktHverUke = null,
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
                timerFasteDager = null
            )
        }
    }

    @Test
    fun `Gir feil ved henting av timer per dag fra snitt dersom timerPerUkeISnitt ikke er satt`(){
        val normalArbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = null,
            timerFasteDager = PlanUkedager()
        )
        assertThrows<IllegalArgumentException> { normalArbeidstid.timerPerDagFraSnitt() }
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerPerUkeISnitt - 37,5 timer per uke gir 7,5 per dag`(){
        val normalarbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
            timerFasteDager = null
        )

        assertTrue(normalarbeidstid.harOppgittTimerSomSnitt())
        assertFalse(normalarbeidstid.harOppgittTimerSomFasteDager())
        assertEquals(Duration.ofHours(7).plusMinutes(30), normalarbeidstid.timerPerDagFraSnitt())
    }

    @Test
    fun `Gir feil ved henting av timer per dag fra fasteDager dersom timerFasteDager ikke er satt`(){
        val normalArbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
            timerFasteDager = null
        )
        assertThrows<IllegalArgumentException> { normalArbeidstid.timerPerDagFraFasteDager(MONDAY) }
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerFasteDager`(){
        val femTimer = Duration.ofHours(5)
        val normalarbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = null,
            timerFasteDager = PlanUkedager(mandag = femTimer, tirsdag = femTimer)
        )
        assertTrue(normalarbeidstid.harOppgittTimerSomFasteDager())
        assertFalse(normalarbeidstid.harOppgittTimerSomSnitt())

        assertEquals(femTimer, normalarbeidstid.timerPerDagFraFasteDager(MONDAY))
        assertEquals(femTimer, normalarbeidstid.timerPerDagFraFasteDager(TUESDAY))

        listOf(WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).forEach { dag ->
            assertEquals(Duration.ZERO, normalarbeidstid.timerPerDagFraFasteDager(dag))
        }
    }

}