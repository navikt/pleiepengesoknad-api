package no.nav.helse.k9format

import no.nav.helse.soknad.Periode
import no.nav.helse.soknad.Utenlandsopphold
import no.nav.helse.soknad.UtenlandsoppholdIPerioden
import no.nav.helse.soknad.Årsak
import no.nav.helse.somJson
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class K9FormatUtenlandsopphold {

    @Test
    fun `UtenlandsoppholdIPerioden med en innleggelse mappes riktig`() {
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-05"),
                            tilOgMed = LocalDate.parse("2022-01-06")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = utenlandsopphold.tilK9Utenlandsopphold().somJson()
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-04": {
                  "land": "DE",
                  "årsak": null
                },
                "2022-01-05/2022-01-06": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                },
                "2022-01-07/2022-01-10": {
                  "land": "DE",
                  "årsak": null
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }

    @Test
    fun `UtenlandsoppholdIPerioden med en innleggelse som dekker hele oppholdet mappes riktig`() {
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-01"),
                            tilOgMed = LocalDate.parse("2022-01-10")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = utenlandsopphold.tilK9Utenlandsopphold().somJson()
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-10": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }

    @Test
    fun `Utenlandsopphold med innleggelser i kant med start og slutt mappes riktig`(){
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-01"),
                            tilOgMed = LocalDate.parse("2022-01-03")
                        ),
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-08"),
                            tilOgMed = LocalDate.parse("2022-01-10")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = utenlandsopphold.tilK9Utenlandsopphold().somJson()
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-03": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                },
                "2022-01-04/2022-01-07": {
                  "land": "DE",
                  "årsak": null
                },
                "2022-01-08/2022-01-10": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }

    @Test
    fun `Utenlandsopphold med flere usammenhengende innleggelser mappes riktig`(){
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-03"),
                            tilOgMed = LocalDate.parse("2022-01-04")
                        ),
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-05"),
                            tilOgMed = LocalDate.parse("2022-01-05")
                        ),
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-07"),
                            tilOgMed = LocalDate.parse("2022-01-07")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = utenlandsopphold.tilK9Utenlandsopphold().somJson()
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-02": {
                  "land": "DE",
                  "årsak": null
                },
                "2022-01-03/2022-01-04": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                },
                "2022-01-05/2022-01-05": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                },
                "2022-01-06/2022-01-06": {
                  "land": "DE",
                  "årsak": null
                },
                "2022-01-07/2022-01-07": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                },
                "2022-01-08/2022-01-10": {
                  "land": "DE",
                  "årsak": null
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }
}