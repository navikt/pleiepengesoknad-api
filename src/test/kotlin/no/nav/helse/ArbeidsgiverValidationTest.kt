package no.nav.helse

import no.nav.helse.soknad.OrganisasjonDetaljer
import no.nav.helse.soknad.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArbeidsgiverValidationTest {

    private companion object {
        private const val GYLDIG_ORGNR = "917755736"
    }

    @Test
    fun `Ingen informasjon om arbeidsuker satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR
        ))
        assertTrue(organisasjoner.validate(true).isEmpty())
    }

    @Test
    fun `Ingen informasjon om arbeidsuker satt, ingen grad satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR
        ))
        assertEquals(1, organisasjoner.validate(false).size)
    }

    @Test
    fun `Skal jobbe prosent satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 99.09
        ))
        assertTrue(organisasjoner.validate(true).isEmpty())
    }

    @Test
    fun `Skal jobbe prosent satt for lav`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = -1.00
        ))
        assertEquals(1, organisasjoner.validate(true).size)
    }

    @Test
    fun `Skal jobbe prosent satt før høy`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 101.01
        ))
        assertEquals(1, organisasjoner.validate(true).size)
    }
}