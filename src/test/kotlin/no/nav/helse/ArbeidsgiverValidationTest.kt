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
            skalJobbe = "ja"
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Ingen informasjon om arbeidsuker satt, ingen grad satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR
        ))
        assertEquals(1, organisasjoner.validate().size)
    }

    @Test
    fun `Skal jobbe prosent satt`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 99.09,
            skalJobbe = "redusert"
        ))
        assertTrue(organisasjoner.validate().isEmpty())
    }

    @Test
    fun `Skal jobbe prosent satt for lav`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = -1.00,
            skalJobbe = "nei"
        ))
        assertEquals(1, organisasjoner.validate().size)
    }

    @Test
    fun `Skal jobbe prosent satt før høy`() {
        val organisasjoner = listOf(OrganisasjonDetaljer(
            organisasjonsnummer = GYLDIG_ORGNR,
            skalJobbeProsent = 101.01,
            skalJobbe = "ja"
        ))
        assertEquals(1, organisasjoner.validate().size)
    }

    @Test
    fun `feiler på søkand der arbeidsgiver skalJobbe er annet enn tillatt verdi`() {
        val organisasjoner = listOf(
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "ja"),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "nei"),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "redusert"),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "vet_ikke"),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "ugyldig1"),
            OrganisasjonDetaljer(organisasjonsnummer = GYLDIG_ORGNR, skalJobbe = "ugyldig2")
        )
        assertEquals(2, organisasjoner.validate().size)
    }
}
