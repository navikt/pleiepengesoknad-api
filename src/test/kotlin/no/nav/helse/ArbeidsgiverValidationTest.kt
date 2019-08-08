package no.nav.helse

import no.nav.helse.soknad.OrganisasjonDetaljer
import no.nav.helse.soknad.validate
import java.time.Duration
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
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Normal og redusert arbeidsuke satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            normalArbeidsuke = Duration.ofHours(7).plusMillis(30),
            redusertArbeidsuke = Duration.ofHours(5)
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Normal arbeidsuke satt, men ikke redusert arbeidsuke`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            normalArbeidsuke = Duration.ofHours(6)
        ))
        assertEquals(1, organisasjoner.validate().size)
    }

    @Test
    fun `Redusert arbeidsuke satt, men ikke normal arbeidsuke`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            normalArbeidsuke = Duration.ofHours(6)
        ))
        assertEquals(1, organisasjoner.validate().size)
    }

    @Test
    fun `Normal arbeidsuke satt til 0`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            normalArbeidsuke = Duration.ZERO
        ))
        assertEquals(1, organisasjoner.validate().size)
    }

    @Test
    fun `Redusert arbeidsuke satt til 0`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            normalArbeidsuke = Duration.ofHours(6),
            redusertArbeidsuke = Duration.ZERO
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Redusert arbeidsuke lengre enn normal arbeidsuke`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            normalArbeidsuke = Duration.ofHours(6),
            redusertArbeidsuke = Duration.ofHours(7)
        ))
        assertEquals(1, organisasjoner.validate().size)
    }
}