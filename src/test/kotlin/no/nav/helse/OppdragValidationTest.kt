package no.nav.helse

import no.nav.helse.soknad.Oppdrag
import no.nav.helse.soknad.harGyldigPeriode
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class OppdragValidationTest {
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
    fun `Er oppdraget pågående`() {
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "montessori",
            erPagaende = true,
            fraOgMed = LocalDate.now().minusDays(30)
        )
        assertTrue(oppdrag.harGyldigPeriode())
    }

/*    @Test
    fun `Hvis pågående så kan ikke tilogmed være satt`(){
        val oppdrag = Oppdrag(
            arbeidsgivernavn = "BariBar",
            erPagaende = true,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(1)
        )

        assertFalse(oppdrag.kanIkkeVaerePaagaendeOgFerdig())
    }*/
}