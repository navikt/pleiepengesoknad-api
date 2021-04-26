package no.nav.helse

import no.nav.helse.soknad.Arbeidsform.TURNUS
import no.nav.helse.soknad.OrganisasjonDetaljer
import no.nav.helse.soknad.SkalJobbe
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
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbe = SkalJobbe.JA,
            jobberNormaltTimer = 37.5,
            skalJobbeProsent = 100.0,
            arbeidsform = TURNUS
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Skal jobbe prosent satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 99.09,
            skalJobbe = SkalJobbe.REDUSERT,
            jobberNormaltTimer = 37.5,
            arbeidsform = TURNUS
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Skal jobbe prosent satt for lav`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = -1.00,
            skalJobbe = SkalJobbe.NEI,
            jobberNormaltTimer = 37.5,
            arbeidsform = TURNUS
        ))
        assertEquals(2, organisasjoner.validate().size)
    }

    @Test
    fun `Skal jobbe prosent satt før høy`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 101.01,
            skalJobbe = SkalJobbe.JA,
            jobberNormaltTimer = 37.5,
            arbeidsform = TURNUS
        ))
        assertEquals(2, organisasjoner.validate().size)
    }
}
