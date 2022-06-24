package no.nav.helse.domene

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.TestUtils.Companion.verifiserIngenFeil
import no.nav.helse.soknad.Land
import no.nav.helse.soknad.domene.Næringstyper
import no.nav.helse.soknad.domene.UtenlandskNæring
import no.nav.helse.soknad.domene.UtenlandskNæring.Companion.valider
import java.time.LocalDate
import kotlin.test.Test

class UtenlandskNæringTest {

    @Test
    fun `Gyldig utenlandskNæring gir ingen valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstyper.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("utenlandskNæring[0]").verifiserIngenFeil()
    }

    @Test
    fun `UtenlandskNæring med ugyldig land gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstyper.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("ABC", " "),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("utenlandskNæring[0]").verifiserFeil(2,
            listOf(
                "utenlandskNæring[0].land.Landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring[0].land.landnavn kan ikke være tomt eller blankt."
            )
        )
    }

    @Test
    fun `UtenlandskNæring med tilOgMed før fraOgMed gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstyper.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-05"),
            tilOgMed = LocalDate.parse("2022-01-01")
        ).valider("utenlandskNæring[0]").verifiserFeil(1,
            listOf("utenlandskNæring[0].tilOgMed må være lik eller etter fraOgMed")
        )
    }

    @Test
    fun `Liste med UtenlandskNæring som har feil i land gir valideringsfeil`(){
        listOf(
            UtenlandskNæring(
                næringstype = Næringstyper.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "Flush AS",
                land = Land("CBA", "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-03")
            ),
            UtenlandskNæring(
                næringstype = Næringstyper.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "Flush AS",
                land = Land("ABC", "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-05")
            )
        ).valider("utenlandskNæring").verifiserFeil(2,
            listOf(
                "utenlandskNæring[0].land.Landkode 'CBA' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring[1].land.Landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }
}