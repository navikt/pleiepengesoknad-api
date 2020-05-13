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
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbe = "ja",
            jobberNormaltTimer = 37.5,
            skalJobbeProsent = 100.0
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Skal jobbe prosent satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 99.09,
            skalJobbe = "redusert",
            jobberNormaltTimer = 37.5
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Skal jobbe prosent satt for lav`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = -1.00,
            skalJobbe = "nei",
            jobberNormaltTimer = 37.5
        ))
        assertEquals(2, organisasjoner.validate().size)
    }

    @Test
    fun `Skal jobbe prosent satt før høy`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 101.01,
            skalJobbe = "ja",
            jobberNormaltTimer = 37.5
        ))
        assertEquals(2, organisasjoner.validate().size)
    }

    @Test
    fun `feiler på søkand der arbeidsgiver skalJobbe er annet enn tillatt verdi`() {
        val organisasjoner = listOf(
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "ja", jobberNormaltTimer = 37.5, skalJobbeProsent = 100.0),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "nei", jobberNormaltTimer = 37.5, skalJobbeProsent = 0.0),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "redusert", jobberNormaltTimer = 37.5, skalJobbeProsent = 50.0),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "vetIkke", jobberNormaltTimer = 37.5, skalJobbeProsent = 0.0),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "ugyldig1", jobberNormaltTimer = -1.0, skalJobbeProsent = 100.0),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "ugyldig2", jobberNormaltTimer = 37.5, skalJobbeProsent = -1.0)
        )
        assertEquals(3, organisasjoner.validate().size)
    }
}
