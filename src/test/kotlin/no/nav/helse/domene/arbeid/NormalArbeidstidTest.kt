package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

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

        assertEquals(Duration.ofHours(7).plusMinutes(30), normalarbeidstid.timerPerDag())
    }

    @Test
    fun `Regner ut riktig timerPerDag fra timerFasteDager`(){
        val femTimer = Duration.ofHours(5).plusMinutes(45)
        val normalarbeidstid = NormalArbeidstid(
            erLiktHverUke = true,
            timerPerUkeISnitt = null,
            timerFasteDager = PlanUkedager(mandag = femTimer, tirsdag = femTimer)
        )

        assertEquals(femTimer, normalarbeidstid.timerPerDag(DayOfWeek.MONDAY))
        assertEquals(femTimer, normalarbeidstid.timerPerDag(DayOfWeek.TUESDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDag(DayOfWeek.WEDNESDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDag(DayOfWeek.THURSDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDag(DayOfWeek.FRIDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDag(DayOfWeek.SATURDAY))
        assertEquals(Duration.ZERO, normalarbeidstid.timerPerDag(DayOfWeek.SUNDAY))
    }

}