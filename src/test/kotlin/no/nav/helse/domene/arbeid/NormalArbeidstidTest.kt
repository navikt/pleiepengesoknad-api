package no.nav.helse.domene.arbeid

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NormalArbeidstidTest {

    @Test
    fun `Gir feil dersom verken timerPerUkeISnitt eller timerFasteDager er satt`() {
        NormalArbeidstid(
            timerPerUkeISnitt = null,
            timerFasteDager = null
        ).valider("test").verifiserFeil(1)
    }

    @Test
    fun `Gir feil dersom timerPerUkeISnitt og timerFasteDager er satt`(){
            NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
                timerFasteDager = PlanUkedager()
            ).valider("test").verifiserFeil(1)
    }

    @Test
    fun `Gir feil ved henting av timer per dag fra snitt dersom timerPerUkeISnitt ikke er satt`(){
        val normalArbeidstid = NormalArbeidstid(
            timerPerUkeISnitt = null,
            timerFasteDager = PlanUkedager()
        )
        assertThrows<IllegalArgumentException> { normalArbeidstid.timerPerDagFraSnitt() }
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerPerUkeISnitt - 37,5 timer per uke gir 7,5 per dag`(){
        val normalarbeidstid = NormalArbeidstid(
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
            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
            timerFasteDager = null
        )
        assertThrows<IllegalArgumentException> { normalArbeidstid.timerPerDagFraFasteDager(MONDAY) }
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerFasteDager`(){
        val femTimer = Duration.ofHours(5)
        val normalarbeidstid = NormalArbeidstid(
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
