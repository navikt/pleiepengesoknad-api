package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.Land
import no.nav.helse.soknad.Virksomhet
import no.nav.helse.soknad.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.helse.soknad.domene.Næringstyper
import no.nav.helse.soknad.validate
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VirksomhetTest {

    val gyldigVirksomhet = Virksomhet(
        næringstyper = listOf(Næringstyper.ANNEN),
        fiskerErPåBladB = false,
        fraOgMed = LocalDate.now().minusDays(1),
        tilOgMed = LocalDate.now(),
        næringsinntekt = 1111,
        navnPåVirksomheten = "TullOgTøys",
        registrertINorge = true,
        organisasjonsnummer = "101010",
        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
        harFlereAktiveVirksomheter = true
    )

    @Test
    fun `FraOgMed kan ikke være før tilOgMed, validate skal returnere en violation`(){
        val virksomhet = gyldigVirksomhet.copy(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().minusDays(1),
        )
        virksomhet.validate().assertFeilPaa(listOf("virksomhet.tilogmed og virksomhet.fraogmed"))
    }

    @Test
    fun `FraOgMed er før tilogmed, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet.copy(
            fraOgMed = LocalDate.now().minusDays(1),
            tilOgMed = LocalDate.now()
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `FraOgMed er lik tilogmed, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet.copy(
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now(),
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så må orgnummer være satt, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet.copy(
            registrertINorge = true,
            organisasjonsnummer = "101010",
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så skal den feile hvis orgnummer ikke er satt, validate skal returnere en violation`(){
        val virksomhet = gyldigVirksomhet.copy(
            organisasjonsnummer = null,
            registrertINorge = true,
        )
        virksomhet.validate().assertFeilPaa(listOf("selvstendingNæringsdrivende.virksomhet.organisasjonsnummer"))
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må registrertIUtlandet være satt til noe, validate skal ikke reagere`(){
        val virksomhet = gyldigVirksomhet.copy(
            registrertINorge = false,
            registrertIUtlandet = Land(
                landkode = "DEU",
                landnavn = "Tyskland"
            )
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må den feile hvis registrertIUtlandet ikke er satt til null, validate skal returnere en violation`(){
        val virksomhet = gyldigVirksomhet.copy(
            registrertINorge = false,
            registrertIUtlandet = null
        )
        virksomhet.validate().assertFeilPaa(listOf("selvstendingNæringsdrivende.virksomhet.registrertIUtlandet"))
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode, validering skal gi feil`(){
        val virksomhet = gyldigVirksomhet.copy(
            registrertINorge = false,
            registrertIUtlandet = Land(
                landnavn = "Tyskland",
                landkode = "NO"
            )
        )
        virksomhet.validate().assertFeilPaa(listOf("selvstendingNæringsdrivende.virksomhet.registrertIUtlandet.landkode"))
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode`(){
        val virksomhet = gyldigVirksomhet.copy(
            registrertINorge = false,
            registrertIUtlandet = Land(
                landnavn = "Tyskland",
                landkode = "DEU"
            )
        )
        virksomhet.validate().assertIngenFeil()
    }

    @Test
    fun `Hvis harFlereAktiveVirksomheter er null skal validering gi feil`(){
        val virksomhet = gyldigVirksomhet.copy(
            harFlereAktiveVirksomheter = null
        )
        virksomhet.validate().assertFeilPaa(listOf("selvstendingNæringsdrivende.virksomhet.harFlereAktiveVirksomheter"))
    }

    private fun MutableSet<Violation>.assertIngenFeil() = assertTrue(isEmpty())

    private fun MutableSet<Violation>.assertFeilPaa(parameterNames: List<String> = emptyList()) {
        assertEquals(size, parameterNames.size)
        forEach {
            assertTrue(parameterNames.contains(it.parameterName))
        }
    }
}

