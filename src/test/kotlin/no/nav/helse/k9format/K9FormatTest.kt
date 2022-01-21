package no.nav.helse.k9format

import no.nav.helse.SøknadUtils
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.Enkeltdag
import no.nav.helse.soknad.Ferieuttak
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.Omsorgsdager
import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.PlanUkedager
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.felles.type.Periode
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class K9FormatTest {

    @Test
    fun `Full PP søknad blir til riktig K9Format`() {
        val mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()
        val fraOgMed = LocalDate.parse("2021-01-01")
        val tilOgMed = LocalDate.parse("2021-01-10")
        val søknad = SøknadUtils.defaultSøknad(søknadId).copy(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = fraOgMed,
                        tilOgMed = fraOgMed.plusDays(1)
                    )
                )
            ),
            omsorgstilbud = Omsorgstilbud(
                historisk = null,
                planlagt = Omsorgsdager(
                    ukedager = null,
                    enkeltdager = listOf(
                        Enkeltdag(
                            fraOgMed.plusDays(1),
                            Duration.ofHours(5)
                        )
                    )
                )
            )
        )
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fødselsnummer = "123456789"
        )
        val k9Format = søknad.tilK9Format(mottatt, søker, dagensDato = LocalDate.parse("2021-01-05"))

        val forventetK9FormatJsonV2 =
            //language=json
            """
            {
              "søknadId": "$søknadId",
              "versjon": "1.0.0",
              "mottattDato": "2020-01-02T03:04:05.000Z",
              "søker": {
                "norskIdentitetsnummer": "123456789"
              },
              "ytelse": {
                "type": "PLEIEPENGER_SYKT_BARN",
                "barn": {
                  "norskIdentitetsnummer": "03028104560",
                  "fødselsdato": null
                },
                "søknadsperiode": [
                  "2021-01-01/2021-01-10"
                ],
                "endringsperiode": [],
                "trekkKravPerioder": [],
                "opptjeningAktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2020-01-01/..": {
                          "virksomhetstyper": [
                            "ANNEN"
                          ],
                          "regnskapsførerNavn": "Kjell Regnskap",
                          "regnskapsførerTlf": "123456789",
                          "erVarigEndring": true,
                          "erNyIArbeidslivet" : true,
                          "endringDato": "2020-01-01",
                          "endringBegrunnelse": "Korona",
                          "bruttoInntekt": 9999,
                          "erNyoppstartet": true,
                          "registrertIUtlandet": true,
                          "landkode": "DEU"
                        }
                      },
                      "virksomhetNavn": "TullOgTøys"
                    }
                  ],
                  "frilanser": {
                    "startdato": "2018-01-01",
                    "sluttdato": null
                  }
                },
                "dataBruktTilUtledning": {
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true,
                  "samtidigHjemme": true,
                  "harMedsøker": true,
                  "bekrefterPeriodeOver8Uker": null
                },
                "infoFraPunsj": null,
                "bosteder": {
                  "perioder": {
                    "2017-01-01/2017-01-10": {
                      "land": "DEU"
                    },
                    "2018-01-01/2018-01-10": {
                      "land": "DEU"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "land": "SE",
                      "årsak": null
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "beredskap": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "tilleggsinformasjon": "Ikke beredskap"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "nattevåk": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "tilleggsinformasjon": "Har nattevåk"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "tilsynsordning": {
                  "perioder": {
                    "2021-01-02/2021-01-02": {
                      "etablertTilsynTimerPerDag": "PT5H"
                    }
                  }
                },
                "lovbestemtFerie": {
                  "perioder": {
                    "$fraOgMed/${fraOgMed.plusDays(1)}": {
                      "skalHaFerie": true
                    }
                  }
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "917755736",
                      "arbeidstidInfo": {
                         "perioder": {
                           "2021-01-01/2021-01-01": {
                             "jobberNormaltTimerPerDag": "PT8H",
                             "faktiskArbeidTimerPerDag": "PT0S"
                           },
                           "2021-01-04/2021-01-04": {
                             "jobberNormaltTimerPerDag": "PT8H",
                             "faktiskArbeidTimerPerDag": "PT7H30M"
                           },
                           "2021-01-05/2021-01-05": {
                               "jobberNormaltTimerPerDag": "PT8H",
                                "faktiskArbeidTimerPerDag": "PT0S"
                           },
                           "2021-01-06/2021-01-06": {
                                "jobberNormaltTimerPerDag": "PT8H",
                                "faktiskArbeidTimerPerDag": "PT0S"
                           },
                           "2021-01-07/2021-01-07": {
                                "jobberNormaltTimerPerDag": "PT8H",
                                "faktiskArbeidTimerPerDag": "PT0S"
                           },
                           "2021-01-08/2021-01-08": {
                                "jobberNormaltTimerPerDag": "PT8H",
                                "faktiskArbeidTimerPerDag": "PT0S"
                           }
                         }
                       }
                    },
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "977155436",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-10": {
                            "jobberNormaltTimerPerDag": "PT0S",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          }
                        }
                      }
                    }
                  ],
                  "frilanserArbeidstidInfo": {
                    "perioder": {
                       "2021-01-01/2021-01-01": {
                         "jobberNormaltTimerPerDag": "PT8H",
                         "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-04/2021-01-04": {
                         "jobberNormaltTimerPerDag": "PT8H",
                         "faktiskArbeidTimerPerDag": "PT7H30M"
                       },
                       "2021-01-05/2021-01-05": {
                           "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-06/2021-01-06": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-07/2021-01-07": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-08/2021-01-08": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       }
                     }
                  },
                  "selvstendigNæringsdrivendeArbeidstidInfo": {
                    "perioder": {
                       "2021-01-01/2021-01-01": {
                         "jobberNormaltTimerPerDag": "PT8H",
                         "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-04/2021-01-04": {
                         "jobberNormaltTimerPerDag": "PT8H",
                         "faktiskArbeidTimerPerDag": "PT7H30M"
                       },
                       "2021-01-05/2021-01-05": {
                           "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-06/2021-01-06": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-07/2021-01-07": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       },
                       "2021-01-08/2021-01-08": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                       }
                     }
                  }
                },
                "uttak": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "timerPleieAvBarnetPerDag": "PT7H30M"
                    }
                  }
                },
                "omsorg": {
                  "relasjonTilBarnet": "ANNET",
                  "beskrivelseAvOmsorgsrollen": "Gudfar til barnet"
                }
              },
              "språk": "nb",
              "journalposter": [],
              "begrunnelseForInnsending": {
                "tekst": null
              }
            }
        """.trimIndent()

        println(JsonUtils.toString(k9Format))
        JSONAssert.assertEquals(forventetK9FormatJsonV2, JsonUtils.toString(k9Format), true)
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn alle dager, forvent 5 perioder`() {
        val k9Tilsynsordning = Omsorgstilbud(
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(5),
                    tirsdag = Duration.ofHours(5),
                    onsdag = Duration.ofHours(5),
                    torsdag = Duration.ofHours(5),
                    fredag = Duration.ofHours(5)
                )
            )
        ).tilK9Tilsynsordning(
            dagensDato = LocalDate.parse("2021-01-04"),
            periode = Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08"))
        )

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
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode ons-man, tilsyn alle dager, forvent 4 perioder med lør-søn ekskludert`() {
        val k9Tilsynsordning = Omsorgstilbud(
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(5),
                    tirsdag = Duration.ofHours(5),
                    onsdag = Duration.ofHours(5),
                    torsdag = Duration.ofHours(5),
                    fredag = Duration.ofHours(5)
                )
            )
        ).tilK9Tilsynsordning(
            dagensDato = LocalDate.parse("2021-01-06"),
            periode = Periode(LocalDate.parse("2021-01-06"), LocalDate.parse("2021-01-11"))
        )

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
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn man-ons og fre, forvent 4 perioder`() {
        val k9Tilsynsordning = Omsorgstilbud(
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(5),
                    tirsdag = Duration.ofHours(5),
                    onsdag = Duration.ofHours(5),
                    torsdag = null,
                    fredag = Duration.ofHours(5)
                )
            )
        ).tilK9Tilsynsordning(
            dagensDato = LocalDate.parse("2021-01-04"),
            periode = Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08"))
        )

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
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, uten tilsyn, forvent 1 periode med 0 timer`() {
        val k9Tilsynsordning =
            tilK9Tilsynsordning0Timer(Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08")))

        assertEquals(1, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT0S"
                }
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn 10t alle dager, forvent 5 perioder med 7t 30m`() {
        val k9Tilsynsordning = Omsorgstilbud(
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(10),
                    tirsdag = Duration.ofHours(10),
                    onsdag = Duration.ofHours(10),
                    torsdag = Duration.ofHours(10),
                    fredag = Duration.ofHours(10)
                )
            )
        ).tilK9Tilsynsordning(
            dagensDato = LocalDate.parse("2021-01-04"),
            periode = Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08"))
        )

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
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt omsorgstilbud med både historisk og planlagte omsorgsdager, forvent riktig mapping`() {
        val tilsynsordning = Omsorgstilbud(
            historisk = Omsorgsdager(
                enkeltdager = listOf(Enkeltdag(LocalDate.now().minusDays(1), Duration.ofHours(7)))
            ),
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(1),
                    tirsdag = Duration.ofHours(1),
                    onsdag = Duration.ofHours(1),
                    torsdag = Duration.ofHours(1),
                    fredag = Duration.ofHours(1)
                )
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.now(), LocalDate.now().plusDays(7)))

        assertEquals(7, tilsynsordning.perioder.size)
    }

    @Test
    fun `gitt omsorgstilbud med både historisk og planlagte omsorgsdager der historisk har dato lik eller etter dagens dato, forvent at den blir eksludert`() {
        val tilsynsordning = Omsorgstilbud(
            historisk = Omsorgsdager(
                enkeltdager = listOf(
                    Enkeltdag(LocalDate.parse("2021-09-01"), Duration.ofHours(7)),
                    Enkeltdag(LocalDate.parse("2021-09-02"), Duration.ofHours(7)),
                    Enkeltdag(LocalDate.parse("2021-09-03"), Duration.ofHours(7))
                )
            ),
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(1),
                    tirsdag = Duration.ofHours(1),
                    onsdag = Duration.ofHours(1),
                    torsdag = Duration.ofHours(1),
                    fredag = Duration.ofHours(1)
                )
            )
        ).tilK9Tilsynsordning(
            Periode(LocalDate.parse("2021-09-03"), LocalDate.parse("2021-09-13")),
            LocalDate.parse("2021-09-03")
        )

        assertEquals(9, tilsynsordning.perioder.size)
    }

    @Test
    fun `Omsorgstilbud med ukedager både historisk og planlagt splitter på dagens dato`(){
        val tilsynsordning = Omsorgstilbud(
            historisk = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(1),
                    tirsdag = Duration.ofHours(1),
                    onsdag = Duration.ofHours(1),
                    torsdag = Duration.ofHours(1),
                    fredag = Duration.ofHours(1)
                )
            ),
            planlagt = Omsorgsdager(
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(5),
                    tirsdag = Duration.ofHours(5),
                    onsdag = Duration.ofHours(5),
                    torsdag = Duration.ofHours(5),
                    fredag = Duration.ofHours(5)
                )
            )
        ).tilK9Tilsynsordning(
            Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08")),
            LocalDate.parse("2021-01-06")
        )

        val forventet = """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT1H"
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
              }
            }
        """.trimIndent()

        assertEquals(forventet, JsonUtils.toString(tilsynsordning))
    }
}
