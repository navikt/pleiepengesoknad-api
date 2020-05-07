package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.*
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TilsynsordningValidationTest {

    private companion object {
        private val ForLangFritekst = """
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst,
            For Lang fritekst, For Lang fritekst, For Lang fritekst, For Lang fritekst
        """.trimIndent()
    }

    @Test
    fun `Tilsynsordning Ja - Gyldig satt`() {
        medGyldigeDagerSatt().validate().assertIngenFeil()
    }

    @Test
    fun `Tilsynsordning Ja - For lang fritekst`() {
        medGyldigeDagerSatt(tilleggsinformasjon = ForLangFritekst).validate()
            .assertFeilPaa(listOf("tilsynsordning.ja.tilleggsinformasjon"))
    }

    @Test
    fun `Tilsynsordning Ja - vetIkke satt`() {
        medGyldigeDagerSatt(
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.erIkkeLagetEnPlan
            )
        ).validate().assertFeilPaa(listOf("tilsynsordning.vetIkke"))
    }

    @Test
    fun `Tilsynsordning Ja - tilleggsinformasjon skal ikke inneholde data i felt`() {
        Tilsynsordning(svar = TilsynsordningSvar.ja, ja = TilsynsordningJa(
            null,
            null,
            null,
            null,
            null,
            ForLangFritekst
        )).validate()
            .forEach { assertNotEquals(ForLangFritekst, it.invalidValue) }
    }

    @Test
    fun `Tilsynsordning Nei - Gyldig satt`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.nei
        ).validate().assertIngenFeil()
    }

    @Test
    fun `Tilsynsordning Nei - ja satt`() {
        medGyldigeDagerSatt(svar = TilsynsordningSvar.nei).validate().assertFeilPaa(listOf("tilsynsordning.ja"))
    }

    @Test
    fun `Tilsynsordning Nei - vetIkke satt`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.nei,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.erIkkeLagetEnPlan
            )
        ).validate().assertFeilPaa(listOf("tilsynsordning.vetIkke"))
    }

    @Test
    fun `Tilsynsordning Vet ikke - Er ikke laget en plan enda`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.erIkkeLagetEnPlan
            )
        ).validate().assertIngenFeil()
    }

    @Test
    fun `Tilsynsordning Vet ikke - Er sporadisk`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.erSporadisk
            )
        ).validate().assertIngenFeil()
    }


    @Test
    fun `Tilsynsordning Vet ikke - Er sporadisk & annet satt`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.erSporadisk,
                annet = "Skulle ikke vært satt"
            )
        ).validate().assertFeilPaa(listOf("tilsynsordning.vetIkke.annet"))
    }

    @Test
    fun `Tilsynsordning Vet ikke - Annet satt`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.annet,
                annet = "Fordi."
            )
        ).validate().assertIngenFeil()
    }

    @Test
    fun `Tilsynsordning Vet ikke - Annet ikke satt`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.annet
            )
        ).validate().assertFeilPaa(listOf("tilsynsordning.vetIkke.annet"))
    }

    @Test
    fun `Tilsynsordning Vet ikke - Annet for lang`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.annet,
                annet = ForLangFritekst
            )
        ).validate().assertFeilPaa(listOf("tilsynsordning.vetIkke.annet"))
    }

    @Test
    fun `Tilsynsordning Vet ikke - annet skal ikke inneholde data i felt`() {
        Tilsynsordning(
            svar = TilsynsordningSvar.vetIkke,
            vetIkke = TilsynsordningVetIkke(
                svar = TilsynsordningVetIkkeSvar.annet,
                annet = ForLangFritekst
            )).validate()
            .forEach { assertNotEquals(ForLangFritekst, it.invalidValue) }
    }

    private fun MutableSet<Violation>.assertFeilPaa(parameterNames: List<String> = emptyList()) {
        assertEquals(size, parameterNames.size)

        forEach {
            assertTrue(parameterNames.contains(it.parameterName))
        }

    }

    private fun MutableSet<Violation>.assertIngenFeil() = assertTrue(isEmpty())

    private fun medGyldigeDagerSatt(
        svar: TilsynsordningSvar = TilsynsordningSvar.ja,
        tilleggsinformasjon: String? = null,
        vetIkke: TilsynsordningVetIkke? = null
    ) = Tilsynsordning(
        svar = svar,
        ja = TilsynsordningJa(
            mandag = Duration.ofHours(9),
            tirsdag = Duration.ofHours(6),
            onsdag = null,
            torsdag = Duration.ZERO,
            fredag = Duration.ofMinutes(50),
            tilleggsinformasjon = tilleggsinformasjon
        ),
        vetIkke = vetIkke
    )
}
