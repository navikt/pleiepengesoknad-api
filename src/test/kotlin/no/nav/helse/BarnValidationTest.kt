package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.validate
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarnValidationTest {

    @Test
    fun `Flere ID'er satt på barnet`() {
        val barn = BarnDetaljer(
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.now().minusDays(10),
            aktørId = "10000001",
            navn = "Navn"
        )
        barn.validate().assertFeilPaa(listOf("barn"))
    }

    @Test
    fun `Når AktørId settes som ID på barnet kreves hverken relasjon til barnet eller navn`() {
        val barn = BarnDetaljer(
            fødselsnummer = null,
            fødselsdato = null,
            aktørId = "10000001",
            navn = null
        )
        barn.validate().assertFeilPaa()
    }


    @Test
    fun `Når Fødselsnummer settes som ID på barnet kreves det relasjon`() {
        val barn = BarnDetaljer(
            fødselsnummer = "02119970078",
            fødselsdato = null,
            aktørId = null,
            navn = null
        )
        barn.validate().assertFeilPaa()
    }

}

private fun MutableSet<Violation>.assertFeilPaa(parameterNames : List<String> = emptyList()) {
    assertEquals(size, parameterNames.size)

    forEach {
        assertTrue(parameterNames.contains(it.parameterName))
    }

}
