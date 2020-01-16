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
    fun `Ingen ID satt på barnet`() {
        val barn = BarnDetaljer(
            fodselsnummer = null,
            fodselsdato = null,
            aktoerId = null,
            navn = null
        )
        barn.validate(null).assertFeilPaa(listOf("relasjon_til_barnet"))
    }

    @Test
    fun `Flere ID'er satt på barnet`() {
        val barn = BarnDetaljer(
            fodselsnummer = "02119970078",
            fodselsdato = LocalDate.now().minusDays(10),
            aktoerId = "10000001",
            navn = "Navn"
        )
        barn.validate(null).assertFeilPaa(listOf("barn"))
    }

    @Test
    fun `Når AktørId settes som ID på barnet kreves hverken relasjon til barnet eller navn`() {
        val barn = BarnDetaljer(
            fodselsnummer = null,
            fodselsdato = null,
            aktoerId = "10000001",
            navn = null
        )
        barn.validate(null).assertFeilPaa()
    }

    @Test
    fun `Om relasjon på barnet og navn sendes samme med Aktør ID må det være gyldige verdier`() {
        val barn = BarnDetaljer(
            fodselsnummer = null,
            fodselsdato = null,
            aktoerId = "10000001",
            navn = SoknadUtils.forLangtNavn()
        )
        barn.validate("").assertFeilPaa(listOf("relasjon_til_barnet","barn.navn"))
    }

    @Test
    fun `Når Fødselsnummer settes som ID på barnet kreves det både relasjon til barnet og navn`() {
        val barn = BarnDetaljer(
            fodselsnummer = "02119970078",
            fodselsdato = null,
            aktoerId = null,
            navn = null
        )
        barn.validate(null).assertFeilPaa(listOf("relasjon_til_barnet","barn.navn"))
    }

    @Test
    fun `Når Alternativ ID settes som ID på barnet kreves det kun relasjon til barnet`() {
        val barn = BarnDetaljer(
            fodselsnummer = null,
            fodselsdato =  LocalDate.now().minusDays(10),
            aktoerId = null,
            navn = null
        )
        barn.validate(null).assertFeilPaa(listOf("relasjon_til_barnet"))
    }

}

private fun MutableSet<Violation>.assertFeilPaa(parameterNames : List<String> = emptyList()) {
    assertEquals(size, parameterNames.size)

    forEach {
        assertTrue(parameterNames.contains(it.parameterName))
    }

}
