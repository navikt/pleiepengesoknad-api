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
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(1),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertFeilPaa(listOf("virksomhet.tilogmed og virksomhet.fraogmed"))
    }

    @Test
    fun `FraOgMed er før tilogmed, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now().minusDays(1),
            tilOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `FraOgMed er lik tilogmed, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så må orgnummer være satt, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så skal den feile hvis orgnummer ikke er satt, validate skal returnere en violation`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            organisasjonsnummer = null,
            registrertINorge = true,
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertFeilPaa(listOf("organisasjonsnummer"))
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må registrertILand være satt til noe, validate skal ikke reagere`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = false,
            registrertILand = "Sverige",
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må den feile hvis registrertILand ikke er satt til noe, validate skal returnere en violation`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = false,
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now())
        )
        virksomhet.validate().assertFeilPaa(listOf("registrertILand"))
    }

    @Test
    fun `Hvis harRevisor er satt til true så må revisor være et revisorobjekt, validering skal ikke reagere`(){
        val virksomhet = Virksomhet(
            naringstype = listOf(Naringstype.ANNET),
            fiskerErPåBladB = false,
            fraOgMed = LocalDate.now(),
            naringsinntekt = 1111,
            navnPaVirksomheten = "TullOgTøys",
            registrertINorge = true,
            organisasjonsnummer = "101010",
            yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
            revisor = Revisor(
                navn = "Kjell",
                telefon = "9898989",
                kanInnhenteOpplysninger = false
            )
        )
        virksomhet.validate().assertIngenFeil()
    }

    private fun MutableSet<Violation>.assertIngenFeil() = assertTrue(isEmpty())

    private fun MutableSet<Violation>.assertFeilPaa(parameterNames: List<String> = emptyList()) {
        assertEquals(size, parameterNames.size)
        forEach {
            assertTrue(parameterNames.contains(it.parameterName))
        }
    }
}

