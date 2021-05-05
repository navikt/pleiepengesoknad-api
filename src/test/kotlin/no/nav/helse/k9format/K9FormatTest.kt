package no.nav.helse.k9format

import no.nav.helse.SøknadUtils
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.Tilsynsordning
import no.nav.helse.soknad.TilsynsordningJa
import no.nav.helse.soknad.TilsynsordningSvar
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.felles.type.Periode
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

class K9FormatTest {

    @Test
    fun `Full PP søknad blir til riktig K9Format`() {
        val mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()
        val søknad = SøknadUtils.defaultSøknad(søknadId).copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-08")
        )
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fødselsnummer = "123456789"
        )
        val k9Format = søknad.tilK9Format(mottatt, søker)
        val forventetK9FormatJson =
            //language=json
            """
            {
              "søknadId" : "$søknadId",
              "versjon" : "1.0.0",
              "mottattDato" : "2020-01-02T03:04:05.000Z",
              "søker" : {
                "norskIdentitetsnummer" : "123456789"
              },
              "språk" : "nb",
              "ytelse" : {
                "type" : "PLEIEPENGER_SYKT_BARN",
                "søknadsperiode" : [
                  "2021-01-04/2021-01-08"
                ],
                "endringsperiode": [],
                "infoFraPunsj": null,
                "dataBruktTilUtledning" : {
                  "harForståttRettigheterOgPlikter" : true,
                  "harBekreftetOpplysninger" : true,
                  "samtidigHjemme" : true,
                  "harMedsøker" : true,
                  "bekrefterPeriodeOver8Uker" : true
                },
                "barn" : {
                  "norskIdentitetsnummer" : "03028104560",
                  "fødselsdato" : "2018-01-01"
                },
                "opptjeningAktivitet" : {
                  "selvstendigNæringsdrivende" : [ {
                    "perioder" : {
                      "2020-01-01/.." : {
                        "virksomhetstyper" : [ "ANNEN" ],
                        "regnskapsførerNavn" : "Kjell Regnskap",
                        "regnskapsførerTlf" : "123456789",
                        "erVarigEndring" : true,
                        "endringDato" : "2020-01-01",
                        "endringBegrunnelse" : "Korona",
                        "bruttoInntekt" : 1111,
                        "erNyoppstartet" : true,
                        "registrertIUtlandet" : true,
                        "landkode" : "DEU"
                      }
                    },
                    "virksomhetNavn" : "TullOgTøys"
                  } ],
                  "frilanser" : {
                    "startdato" : "2018-01-01",
                    "sluttdato": null,
                    "jobberFortsattSomFrilans" : true
                  }
                },
                "beredskap" : {
                  "perioderSomSkalSlettes": {},
                  "perioder" : {
                    "2021-01-04/2021-01-08" : {
                      "tilleggsinformasjon" : "Ikke beredskap"
                    }
                  }
                },
                "nattevåk" : {
                  "perioderSomSkalSlettes": {},
                  "perioder" : {
                    "2021-01-04/2021-01-08" : {
                      "tilleggsinformasjon" : "Har nattevåk"
                    }
                  }
                },
                "tilsynsordning" : {
                  "perioder" : {
                    "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
                }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "arbeidstid" : {
                  "arbeidstakerList" : [ {
                    "norskIdentitetsnummer" : null,
                    "organisasjonsnummer" : "917755736",
                    "arbeidstidInfo" : {
                      "perioder" : {
                        "2021-01-04/2021-01-08" : {
                          "jobberNormaltTimerPerDag" : "PT8H",
                          "faktiskArbeidTimerPerDag" : "PT3H12M"
                        }
                      }
                    }
                  } ],
                  "frilanserArbeidstidInfo" : null,
                  "selvstendigNæringsdrivendeArbeidstidInfo" : null
                },
                "uttak" : {
                  "perioder" : {
                    "2021-01-04/2021-01-08" : {
                      "timerPleieAvBarnetPerDag" : "PT7H30M"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "omsorg" : {
                  "relasjonTilBarnet" : "ANNET",
                  "beskrivelseAvOmsorgsrollen" : "Gudfar til barnet"
                },
                "lovbestemtFerie" : {
                  "perioder": {
                    "2020-01-05/2020-01-07": {}
                  },
                  "perioderSomSkalSlettes": {}
                },
                "bosteder" : {
                  "perioderSomSkalSlettes": {},
                  "perioder" : {
                    "2017-01-01/2017-01-10" : {
                      "land" : "DEU"
                    },
                    "2018-01-01/2018-01-10" : {
                      "land" : "DEU"
                    }
                  }
                },
                "utenlandsopphold" : {
                  "perioder" : {
                    "2021-01-04/2021-01-08" : {
                      "land" : "SE",
                      "årsak" : null
                    }
                  },
                  "perioderSomSkalSlettes": {}
                }
              }
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9FormatJson, JsonUtils.toString(k9Format), true)
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn alle dager, forvent 5 perioder`() {
        val k9Tilsynsordning = Tilsynsordning(
            svar = TilsynsordningSvar.ja,
            ja = TilsynsordningJa(
                mandag = Duration.ofHours(5),
                tirsdag = Duration.ofHours(5),
                onsdag = Duration.ofHours(5),
                torsdag = Duration.ofHours(5),
                fredag = Duration.ofHours(5),
                tilleggsinformasjon = null
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08")))

        assertEquals(5, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true)
    }

    @Test
    fun `gitt søknadsperiode ons-man, tilsyn alle dager, forvent 4 perioder med lør-søn ekskludert`() {
        val k9Tilsynsordning = Tilsynsordning(
            svar = TilsynsordningSvar.ja,
            ja = TilsynsordningJa(
                mandag = Duration.ofHours(5),
                tirsdag = Duration.ofHours(5),
                onsdag = Duration.ofHours(5),
                torsdag = Duration.ofHours(5),
                fredag = Duration.ofHours(5),
                tilleggsinformasjon = null
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.parse("2021-01-06"), LocalDate.parse("2021-01-11")))

        assertEquals(4, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-11/2021-01-11" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn man-ons og fre, forvent 4 perioder`() {
        val k9Tilsynsordning = Tilsynsordning(
            svar = TilsynsordningSvar.ja,
            ja = TilsynsordningJa(
                mandag = Duration.ofHours(5),
                tirsdag = Duration.ofHours(5),
                onsdag = Duration.ofHours(5),
                null,
                fredag = Duration.ofHours(5),
                tilleggsinformasjon = null
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08")))

        assertEquals(4, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn 10t alle dager, forvent 5 perioder med 7t 30m`() {
        val k9Tilsynsordning = Tilsynsordning(
            svar = TilsynsordningSvar.ja,
            ja = TilsynsordningJa(
                mandag = Duration.ofHours(10),
                tirsdag = Duration.ofHours(10),
                onsdag = Duration.ofHours(10),
                torsdag = Duration.ofHours(10),
                fredag = Duration.ofHours(10),
                tilleggsinformasjon = null
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08")))

        assertEquals(5, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }
}
