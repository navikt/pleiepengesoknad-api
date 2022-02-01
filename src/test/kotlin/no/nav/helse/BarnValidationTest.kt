package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.validate
import no.nav.helse.soknad.ÅrsakManglerIdentitetsnummer
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarnValidationTest {
    val gyldigBarn = BarnDetaljer(
        fødselsnummer = "02119970078",
        fødselsdato = LocalDate.parse("2021-01-01"),
        aktørId = "10000001",
        navn = "Barnesen",
        årsakManglerIdentitetsnummer = null
    )

    @Test
    fun `Gyldig barn gir ingen feil`(){
        gyldigBarn.validate().assertIngenFeil()
    }

    @Test
    fun `Når AktørId settes som ID på barnet kreves hverken relasjon til barnet eller navn`() {
        val barn = gyldigBarn.copy(
            aktørId = "10000001",
            navn = null
        )
        barn.validate().assertIngenFeil()
    }

    @Test
    fun `Skal ikke gi feil selvom fødselsnummer er null så lenge fødselsdato og årsak er satt`() {
        val barn = gyldigBarn.copy(
            fødselsnummer = null,
            fødselsdato = LocalDate.parse("2021-01-01"),
            årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT
        )
        barn.validate().assertIngenFeil()
    }

    @Test
    fun `Skal gi feil dersom fødselsnummer ikke settes og man ikke har satt fødsesldato og årsak`() {
        val barn = gyldigBarn.copy(
            fødselsnummer = null,
            årsakManglerIdentitetsnummer = null,
            fødselsdato = null
        )
        barn.validate().assertFeilPaa(listOf("barn.fødselsdato", "barn.årsakManglerIdentitetsnummer"))
    }

    @Test
    fun `Skal gi feil dersom fødselsdato er i fremtiden`() {
        val barn = gyldigBarn.copy(
            fødselsdato = LocalDate.now().plusDays(1)
        )
        barn.validate().assertFeilPaa(listOf("barn.fødselsdato"))
    }

}

private fun MutableSet<Violation>.assertFeilPaa(parameterNames : List<String> = emptyList()) {
    assertEquals(size, parameterNames.size)

    forEach {
        assertTrue(parameterNames.contains(it.parameterName))
    }
}

private fun MutableSet<Violation>.assertIngenFeil() {
    assertTrue(size == 0)
}
