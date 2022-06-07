package no.nav.helse.validering

import no.nav.helse.soknad.Land
import no.nav.helse.soknad.domene.OpptjeningIUtlandet
import no.nav.helse.soknad.domene.OpptjeningType
import no.nav.helse.soknad.domene.valider
import java.time.LocalDate
import kotlin.test.Test

class OpptjeningIUtlandetValideringsTest {
    val opptjeningIUtlandet = OpptjeningIUtlandet(
        navn = "Kiwi AS",
        opptjeningType = OpptjeningType.ARBEIDSTAKER,
        land = Land(
            landkode = "BEL",
            landnavn = "Belgia",
        ),
        fraOgMed = LocalDate.parse("2022-01-01"),
        tilOgMed = LocalDate.parse("2022-01-10")
    )

    @Test
    fun `Gyldig opptjeningIUtlandet gir ingen feil`() {
        listOf(opptjeningIUtlandet).valider().assertIngenFeil()
    }

    @Test
    fun `Ugyldig landnavn`(){
        listOf(
            opptjeningIUtlandet.copy(
                land = Land(
                    landkode = "BEL",
                    landnavn = " ",
                )
            )
        ).valider().assertFeilPå(listOf("Landnavn kan ikke være blank."))
    }

    @Test
    fun `Ugyldig landkode`(){
        listOf(
            opptjeningIUtlandet.copy(
                land = Land(
                    landkode = "UGYLDIG",
                    landnavn = "Belgia",
                )
            )
        ).valider().assertFeilPå(listOf("Landkode er ikke en gyldig ISO 3166-1 alpha-3 kode."))
    }

}
