package no.nav.helse

import no.nav.helse.soker.Soker
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SokerTest {
    @Test
    fun `En person som fyller 18 i morgen kan ikke søke`() {
        val soker = Soker(
            fodselsnummer = "29099012345",
            fodselsdato = LocalDate.now().minusYears(18).plusDays(1)
        )
        assertFalse(soker.myndig)
    }

    @Test
    fun `En person som fyller 18 i dag kan søke`() {
        val soker = Soker(
            fodselsnummer = "29099012345",
            fodselsdato = LocalDate.now().minusYears(18)
        )
        assertTrue(soker.myndig)
    }
}