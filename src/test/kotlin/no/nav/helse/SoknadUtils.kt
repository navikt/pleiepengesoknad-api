package no.nav.helse

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.soknad.Virksomhet


class SoknadUtils {
    companion object {
        fun forLangtNavn() =
            "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

        fun bodyMedFodselsnummerPaaBarn(
            fodselsnummer: String,
            fraOgMed: String? = "2018-10-10",
            tilOgMed: String? = "2019-10-10",
            vedleggUrl1: String,
            vedleggUrl2: String
        ): String {
            //language=JSON
            return """
                {
                    "barn": {
                        "fodselsnummer": "$fodselsnummer",
                        "navn": "Barn Barnesen"
                    },
                    "relasjonTilBarnet": "mor",
                    "fraOgMed": "$fraOgMed",
                    "tilOgMed": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbeProsent": 50,
                                "skalJobbe": "redusert"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1",
                        "$vedleggUrl2"
                    ],
                    "medlemskap" : {
                        "harBoddIUtlandetSiste12Mnd" : false,
                        "skalBoIUtlandetNeste12Mnd" : true
                    },
                        "utenlandsoppholdIPerioden": {
                            "skalOppholdeSegIUtlandetIPerioden": true,
                            "opphold": [
                                {
                                    "fraOgMed": "2019-10-10",
                                    "tilOgMed": "2019-11-10",
                                    "landkode": "SE",
                                    "landnavn": "Sverige"
                                },
                                {
                                    "landnavn": "USA",
                                    "landkode": "US",
                                    "fraOgMed": "2020-01-08",
                                    "tilOgMed": "2020-01-09",
                                    "erUtenforEos": true,
                                    "erBarnetInnlagt": true,
                                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                                }
                            ]
                        },
                        "dagerPerUkeBorteFraJobb": 4.0,
                    "harMedsøker": true,
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "ferieuttakIPerioden": {
                    "skalTaUtFerieIPeriode": true,
                    "ferieuttak": [
                      {
                        "fraOgMed": "2020-01-05",
                        "tilOgMed": "2020-01-07"
                      }
                    ]
                  },
                  "skalBekrefteOmsorg": true,
                  "skalPassePaBarnetIHelePerioden": true,
                  "beskrivelseOmsorgsrollen": "En kort beskrivelse"
                }
                """.trimIndent()
        }

        fun bodyMedAktoerIdPaaBarn(
            aktørId: String,
            vedleggUrl1: String,
            vedleggUrl2: String
        ): String {
            return """
                {
                    "barn": {
                        "aktørId": "$aktørId"
                    },
                    "fraOgMed": "2018-10-10",
                    "tilOgMed": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbe": "nei"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1",
                        "$vedleggUrl2"
                    ],
                    "medlemskap" : {
                        "harBoddIUtlandetSiste12Mnd" : false,
                        "skalBoIUtlandetNeste12Mnd" : true
                    },
                    "utenlandsopphold_i_perioden": {
                        "skalOppholdeSegIUtlandetIPerioden": false,
                        "opphold": []
                    },
                    "harMedsøker": false,
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                  "ferieuttakIPerioden": {
                    "skalTaUtFerieIPeriode": true,
                    "ferieuttak": [
                      {
                        "fraOgMed": "2020-01-05",
                        "tilOgMed": "2020-01-07"
                      }
                    ]
                  }
                }
                }
                """.trimIndent()
        }

        fun bodyMedSelvstendigVirksomheterSomListe(vedleggUrl1: String, virksomheter: List<Virksomhet>): String {
            val virksomheterSomJson = jacksonObjectMapper().dusseldorfConfigured()
                .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .writerWithDefaultPrettyPrinter().writeValueAsString(virksomheter)

            return """
                {
                    "barn": {},
                    "relasjonTilBarnet": "mor",
                    "fraOgMed": "2018-10-10",
                    "tilOgMed": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbe": "nei"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1"
                    ],
                    "medlemskap" : {
                        "harBoddIUtlandetSiste12Mnd" : false,
                        "skalBoIUtlandetNeste12Mnd" : true
                    },
                    "utenlandsoppholdIPerioden": {
                        "skalOppholdeSegIUtlandetIPerioden": false,
                        "opphold": []
                    },
                    "harMedsøker": true,
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "ferieuttakIPerioden": {
                        "skalTaUtFerieIPeriode": true,
                        "ferieuttak": [
                          {
                            "fraOgMed": "2020-01-02",
                            "tilOgMed": "2020-01-07"
                          }
                        ]
                    },
                    "harHattInntektSomSelvstendigNaringsdrivende" : true,
                    "selvstendigVirksomheter" : $virksomheterSomJson
                    }
            """.trimIndent()
        }

        fun bodyMedSelvstendigVirksomhet(vedleggUrl1: String): String {
            return """
                {
                  "barn": {
                    
                  },
                  "relasjonTilBarnet": "mor",
                  "fraOgMed": "2018-10-10",
                  "tilOgMed": "2019-10-10",
                  "arbeidsgivere": {
                    "organisasjoner": [
                      {
                        "organisasjonsnummer": "917755736",
                        "navn": "Bjeffefirmaet ÆÆÅ",
                        "skalJobbe": "nei"
                      }
                    ]
                  },
                  "vedlegg": [
                    "$vedleggUrl1"
                  ],
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": false,
                    "skalBoIUtlandetNeste12Mnd": true
                  },
                  "utenlandsoppholdIPerioden": {
                    "skalOppholdeSegIUtlandetIPerioden": false,
                    "opphold": [
                      
                    ]
                  },
                  "harMedsøker": true,
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "ferieuttakIPerioden": {
                    "skalTaUtFerieIPeriode": true,
                    "ferieuttak": [
                      {
                        "fraOgMed": "2020-01-02",
                        "tilOgMed": "2020-01-07"
                      }
                    ]
                  },
                  "harHattInntektSomSelvstendigNaringsdrivende": true,
                  "selvstendig_virksomheter": [
                    {
                      "næringstyper": [
                        "JORDBRUK_SKOGBRUK"
                      ],
                      "fiskerErPåBladB": false,
                      "fraOgMed": "2020-04-27",
                      "tilOgMed": "2020-04-28",
                      "næringsinntekt": 1212,
                      "navnPåVirksomheten": "TullOgTøys",
                      "organisasjonsnummer": null,
                      "registrertINorge": false,
                      "registrertIUtlandet": null,
                      "yrkesaktivSisteTreFerdigliknedeÅrene": {
                        "oppstartsdato": "2020-04-28"
                      },
                      "varigEndring": null,
                      "regnskapsfører": null,
                      "revisor": null
                    },
                    {
                      "næringstyper": [
                        "JORDBRUK_SKOGBRUK"
                      ],
                      "fiskerErPåBladB": false,
                      "fraOgMed": "2020-04-27",
                      "tilOgMed": "2020-04-28",
                      "næringsinntekt": 1212,
                      "navnPåVirksomheten": "BariBar",
                      "organisasjonsnummer": "10110",
                      "registrertINorge": true,
                      "registrertIUtlandet": null,
                      "yrkesaktivSisteTreFerdigliknedeÅrene": {
                        "oppstartsdato": "2020-04-28"
                      },
                      "varigEndring": null,
                      "regnskapsfører": null,
                      "revisor": null
                    }
                  ]
                }
            """.trimIndent()
        }

        fun bodyUtenIdPaaBarn(
            vedleggUrl1: String,
            vedleggUrl2: String
        ): String {
            return """
                {
                    "barn": {},
                    "relasjonTilBarnet": "mor",
                    "fraOgMed": "2018-10-10",
                    "tilOgMed": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbe": "nei"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1",
                        "$vedleggUrl2"
                    ],
                    "medlemskap" : {
                        "harBoddIUtlandetSiste12Mnd" : false,
                        "skalBoIUtlandetNeste12Mnd" : true
                    },
                    "utenlandsoppholdIPerioden": {
                        "skalOppholdeSegIUtlandetIPerioden": false,
                        "opphold": []
                    },
                    "harMedsøker": true,
                    "dagerPerUkeBorteFraJobb": 5.0,
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "ferieuttakIPerioden": {
                        "skalTaUtFerieIPeriode": true,
                        "ferieuttak": [
                          {
                            "fraOgMed": "2020-01-05",
                            "tilOgMed": "2020-01-07"
                          }
                        ]
                      }
                    }
                }
                """.trimIndent()
        }

        fun bodyMedJusterbarOrganisasjon(
            fodselsnummer: String,
            fraOgMed: String? = "2018-10-10",
            tilOgMed: String? = "2019-10-10",
            vedleggUrl1: String,
            skalJobbe: String,
            skalJobbeProsent: Double
        ): String {
            //language=JSON
            return """
                {
                    "barn": {
                        "fodselsnummer": "$fodselsnummer",
                        "navn": "Barn Barnesen"
                    },
                    "relasjonTilBarnet": "mor",
                    "fraOgMed": "$fraOgMed",
                    "tilOgMed": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbeProsent": $skalJobbeProsent,
                                "skalJobbe": "$skalJobbe"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1",
                        "$vedleggUrl1"
                    ],
                    "medlemskap" : {
                        "harBoddIUtlandetSiste12Mnd" : false,
                        "skalBoIUtlandetNeste12Mnd" : true
                    },
                        "utenlandsoppholdIPerioden": {
                            "skalOppholdeSegIUtlandetIPerioden": true,
                            "opphold": [
                                {
                                    "fraOgMed": "2019-10-10",
                                    "tilOgMed": "2019-11-10",
                                    "landkode": "SE",
                                    "landnavn": "Sverige"
                                },
                                {
                                    "landnavn": "USA",
                                    "landkode": "US",
                                    "fraOgMed": "2020-01-08",
                                    "tilOgMed": "2020-01-09",
                                    "erUtenforEos": true,
                                    "erBarnetInnlagt": true,
                                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                                }
                            ]
                        },
                    "harMedsøker": true,
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                  "ferieuttakIPerioden": {
                    "skalTaUtFerieIPeriode": true,
                    "ferieuttak": [
                      {
                        "fraOgMed": "2020-01-05",
                        "tilOgMed": "2020-01-07"
                      }
                    ]
                  },
                  "skalBekrefteOmsorg": true,
                  "skalPassePaBarnetIHelePerioden": true,
                  "beskrivelseOmsorgsrollen": "En kort beskrivelse"
                }
                """.trimIndent()
        }

        fun bodyMedJusterbarTilOgFraOgBekrefterPeriodeOver8Uker(
            fraOgMed: String? = "2018-10-10",
            tilOgMed: String? = "2019-10-10",
            bekrefterPeriodeOver8Uker: Boolean = true,
            vedleggUrl1: String
        ): String {
            //language=JSON
            return """
                {
                    "barn": {},
                    "relasjonTilBarnet": "mor",
                    "fraOgMed": "$fraOgMed",
                    "tilOgMed": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                 "organisasjonsnummer": "917755736",
                                  "navn": "Bjeffefirmaet ÆÆÅ",
                                  "skalJobbe": "nei"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1",
                        "$vedleggUrl1"
                    ],
                    "medlemskap" : {
                        "harBoddIUtlandetSiste12Mnd" : false,
                        "skalBoIUtlandetNeste12Mnd" : true
                    },
                        "utenlandsoppholdIPerioden": {
                            "skalOppholdeSegIUtlandetIPerioden": true,
                            "opphold": [
                                {
                                    "fraOgMed": "2019-10-10",
                                    "tilOgMed": "2019-11-10",
                                    "landkode": "SE",
                                    "landnavn": "Sverige"
                                },
                                {
                                    "landnavn": "USA",
                                    "landkode": "US",
                                    "fraOgMed": "2020-01-08",
                                    "tilOgMed": "2020-01-09",
                                    "erUtenforEos": true,
                                    "erBarnetInnlagt": true,
                                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                                }
                            ]
                        },
                    "harMedsøker": true,
                    "bekrefterPeriodeOver8Uker": "$bekrefterPeriodeOver8Uker",
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                  "ferieuttakIPerioden": {
                    "skalTaUtFerieIPeriode": true,
                    "ferieuttak": [
                      {
                        "fraOgMed": "2020-01-05",
                        "tilOgMed": "2020-01-07"
                      }
                    ]
                  },
                  "skalBekrefteOmsorg": true,
                  "skalPassePaBarnetIHelePerioden": true,
                  "beskrivelseOmsorgsrollen": "En kort beskrivelse"
                }
                """.trimIndent()
        }
    }
}
