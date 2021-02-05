package no.nav.helse.k9format

import no.nav.helse.SøknadUtils
import no.nav.helse.soker.Søker
import no.nav.k9.søknad.JsonUtils
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class K9FormatTest {

    @Test
    fun `Full PP søknad blir til riktig K9Format`(){
        val mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()
        val søknad = SøknadUtils.defaultSøknad(søknadId)
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fødselsnummer = "123456789"
        )
        val k9Format = søknad.tilK9Format(mottatt, søker)
        val forventetK9FormatJson = """
            {
              "søknadId" : "$søknadId",
              "versjon" : "1.0",
              "språk" : "nb",
              "mottattDato" : "2020-01-02T03:04:05.000Z",
              "søker" : {
                "norskIdentitetsnummer" : "123456789"
              },
              "ytelse" : {
                "type" : "PLEIEPENGER_SYKT_BARN",
                "søknadsperiode" : "2020-01-01/2020-01-20",
                "søknadInfo" : {
                  "relasjonTilBarnet" : "Forelder",
                  "samtykketOmsorgForBarnet" : true,
                  "beskrivelseAvOmsorgsrollen" : "En kort beskrivelse",
                  "harForståttRettigheterOgPlikter" : true,
                  "harBekreftetOpplysninger" : true,
                  "flereOmsorgspersoner" : null,
                  "samtidigHjemme" : true,
                  "harMedsøker" : true,
                  "bekrefterPeriodeOver8Uker" : true
                },
                "barn" : {
                  "norskIdentitetsnummer" : "123456789",
                  "fødselsdato" : "2018-01-01"
                },
                "arbeidAktivitet" : {
                  "arbeidstaker" : [ {
                    "norskIdentitetsnummer" : "123456789",
                    "organisasjonsnummer" : "917755736",
                    "arbeidstidInfo" : {
                      "jobberNormaltTimerPerDag" : "PT8H",
                      "perioder" : {
                        "2020-01-01/2020-01-20" : {
                          "faktiskArbeidTimerPerDag" : "PT3H12M"
                        }
                      }
                    }
                  } ],
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
                        "erNyoppstartet" : false,
                        "registrertIUtlandet" : true,
                        "landkode" : "DEU"
                      }
                    },
                    "virksomhetNavn" : "TullOgTøys"
                  } ],
                  "frilanser" : {
                    "startdato" : "2018-01-01",
                    "jobberFortsattSomFrilans" : true
                  }
                },
                "beredskap" : {
                  "perioder" : {
                    "2020-01-01/2020-01-20" : {
                      "tilleggsinformasjon" : "Ikke beredskap"
                    }
                  }
                },
                "nattevåk" : {
                  "perioder" : {
                    "2020-01-01/2020-01-20" : {
                      "tilleggsinformasjon" : "Har nattevåk"
                    }
                  }
                },
                "tilsynsordning" : {
                  "perioder" : {
                    "2020-01-01/2020-01-20" : {
                      "etablertTilsynTimerPerDag" : "PT1H"
                    }
                  }
                },
                "arbeidstid" : {
                  "arbeidstakerList" : [ {
                    "norskIdentitetsnummer" : "123456789",
                    "organisasjonsnummer" : "917755736",
                    "arbeidstidInfo" : {
                      "jobberNormaltTimerPerDag" : "PT8H",
                      "perioder" : {
                        "2020-01-01/2020-01-20" : {
                          "faktiskArbeidTimerPerDag" : "PT3H12M"
                        }
                      }
                    }
                  } ],
                  "frilanserArbeidstidInfo" : {
                    "jobberNormaltTimerPerDag" : null,
                    "perioder" : {
                      "2020-01-01/2020-01-20" : {
                        "faktiskArbeidTimerPerDag" : null
                      }
                    }
                  },
                  "selvstendigNæringsdrivendeArbeidstidInfo" : {
                    "jobberNormaltTimerPerDag" : null,
                    "perioder" : {
                      "2020-01-01/.." : {
                        "faktiskArbeidTimerPerDag" : null
                      }
                    }
                  }
                },
                "uttak" : null,
                "lovbestemtFerie" : null,
                "bosteder" : {
                  "perioder" : {
                    "2018-01-01/2018-01-10" : {
                      "land" : "DEU"
                    },
                    "2017-01-01/2017-01-10" : {
                      "land" : "DEU"
                    }
                  }
                },
                "utenlandsopphold" : {
                  "perioder" : {
                    "2020-01-01/2020-01-20" : {
                      "land" : "SE",
                      "årsak" : null
                    }
                  }
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventetK9FormatJson, JsonUtils.toString(k9Format), true)
    }

}