package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.*
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OppdragValidationTest {

    @Test
    fun `Validering slår ikke inn på korrekt oppdrag`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(30)
        )
        oppdrag.validate().assertIngenFeil()
    }

    @Test
    fun `Validerer oppdrag som har lik fraogmed og tilogmed dato`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = false,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now()
        )
        oppdrag.validate().assertIngenFeil()
    }

    @Test
    fun `Validering slår inn på oppdrag uten gyldig arbeidsgivernavn`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "  ",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(30)
        )
        oppdrag.validate().assertFeilPaa(listOf("oppdrag.arbeidsgivernavn"))
    }

    @Test
    fun `Validering slår inn på oppdrag med startdato etter sluttdato`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = false,
            fraOgMed = LocalDate.now().minusDays(1),
            tilOgMed = LocalDate.now().minusDays(10)
        )
        oppdrag.validate().assertFeilPaa(listOf("oppdrag.tilogmed og oppdrag.fraogmed"))
    }

    @Test
    fun `Validering slår inn på oppdrag hvor erPagaende er satt og tilogmed er satt`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(1),
            tilOgMed = LocalDate.now()
        )
        oppdrag.validate().assertFeilPaa(listOf("oppdrag.erPagaende"))
    }


    @Test
    fun `Er start dato før slutt dato`() {
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "montessori",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(30)
        )
        assertTrue(oppdrag.harGyldigPeriode())
    }

    @Test
    fun `Sluttdato satt til før startdato`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = false,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(1)
        )
        assertFalse(oppdrag.harGyldigPeriode())
    }

    @Test
    fun `Er oppdraget pågående`() {
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "montessori",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(30)
        )
        assertTrue(oppdrag.harGyldigPeriode())
    }

   @Test
    fun `Hvis pågående så kan ikke sluttdato være satt`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = true,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(1)
        )
        assertFalse(oppdrag.erPaagaendeErGyldig())
    }

    @Test
    fun `Hvis ikke pågående må sluttdato være satt`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = false,
            fraOgMed = LocalDate.now().minusDays(3),
            tilOgMed = LocalDate.now()
        )
        assertTrue(oppdrag.erPaagaendeErGyldig())
    }

    @Test
    fun `Oppdrag kan ikke ha blank arbeidsgivernavn`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = " ",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(1)
        )
        assertFalse(oppdrag.harGyldigArbeidsgivernavn())
    }

    @Test
    fun `Oppdrag har korrekt arbeidsgivernavn`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "Heftig arbeidsgiver",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(1)
        )
        assertTrue(oppdrag.harGyldigArbeidsgivernavn())
    }

    private fun MutableSet<Violation>.assertIngenFeil() = assertTrue(isEmpty())

    private fun MutableSet<Violation>.assertFeilPaa(parameterNames: List<String> = emptyList()) {
        assertEquals(size, parameterNames.size)

        forEach {
            assertTrue(parameterNames.contains(it.parameterName))
        }

    }
}

