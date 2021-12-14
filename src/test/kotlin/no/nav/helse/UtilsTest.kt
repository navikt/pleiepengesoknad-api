package no.nav.helse

import no.nav.helse.soknad.tilK9MellomLagringUrl
import java.net.URI
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun `Omgjøre vedleggurl til k9-mellomlagringingress med id`(){
        val vedleggUrlsFraInnsending = listOf(URL("https://pleiepenger-api.dev.nav.no/vedlegg/testID.testID"))
        val forventetUrls = listOf(URL("https://k9-mellomlagring.dev.intern.nav.no/v1/dokument/testID.testID"))

        val vedleggFix = vedleggUrlsFraInnsending.tilK9MellomLagringUrl(URI("https://k9-mellomlagring.dev.intern.nav.no/v1/dokument"))
        assertEquals(forventetUrls, vedleggFix)
    }
}