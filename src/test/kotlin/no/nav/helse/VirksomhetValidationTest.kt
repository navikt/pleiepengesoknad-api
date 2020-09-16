package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.*
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VirksomhetTest {

    @Test
    fun `FraOgMed kan ikke være før tilOgMed, validate skal returnere en violation`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(1),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertFeilPaa(listOf("virksomhet.tilogmed og virksomhet.fraogmed"))
    }

    @Test
    fun `FraOgMed er før tilogmed, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now().minusDays(1),
            tilOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertIngenFeil()
    }

    @Test
    fun `FraOgMed er lik tilogmed, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så må orgnummer være satt, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så skal den feile hvis orgnummer ikke er satt, validate skal returnere en violation`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            organisasjonsnummer = null,
            registrertINorge = true,
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertFeilPaa(listOf("selvstendigVirksomheter[0].organisasjonsnummer"))
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må registrertIUtlandet være satt til noe, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = false,
            registrertIUtlandet = Land(
                landkode = "DEU",
                landnavn = "Tyskland"
            ),
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må den feile hvis registrertIUtlandet ikke er satt til noe, validate skal returnere en violation`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = false,
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertFeilPaa(listOf("selvstendigVirksomheter[0].registrertIUtlandet"))
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode, validering skal gi feil`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = false,
            registrertIUtlandet = Land(
                landnavn = "Tyskland",
                landkode = "NO"
            ),
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertFeilPaa(listOf("selvstendigVirksomheter[0].registrertIUtlandet.landkode"))
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode`(){
        val virksomhet = Virksomhet(
            næringstyper = listOf(Næringstyper.ANNEN),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            næringsinntekt = 1111,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = false,
            registrertIUtlandet = Land(
                landnavn = "Tyskland",
                landkode = "DEU"
            ),
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
        )
        virksomhet.validate(0).assertIngenFeil()
    }

    private fun MutableSet<Violation>.assertIngenFeil() = assertTrue(isEmpty())

    private fun MutableSet<Violation>.assertFeilPaa(parameterNames: List<String> = emptyList()) {
        assertEquals(size, parameterNames.size)
        forEach {
            assertTrue(parameterNames.contains(it.parameterName))
        }
    }
}

