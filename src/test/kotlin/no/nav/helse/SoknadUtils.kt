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

        internal val objectMapper = jacksonObjectMapper().pleiepengesøknadKonfigurert()
        internal val objectMapperGammeltFormat = jacksonObjectMapper().dusseldorfConfigured()

        fun bodyMedsnake_caseOgUtenÆØÅ(fodselsnummer: String, vedleggUrl1: String): String{
           return """
                {
                  "new_version": true,
                  "sprak": "nb",
                  "barn": {
                    "navn": "Lille",
                    "fodselsnummer": "$fodselsnummer",
                    "aktoer_id": null,
                    "fodselsdato": "2020-04-17",
                    "samme_adresse": null
                  },
                  "arbeidsgivere": {
                    "organisasjoner": [
                      
                    ]
                  },
                  "medlemskap": {
                    "har_bodd_i_utlandet_siste_12_mnd": true,
                    "skal_bo_i_utlandet_neste_12_mnd": false,
                    "utenlandsopphold_siste_12_mnd": [
                      {
                        "landnavn": "Bahamas",
                        "landkode": "BHS",
                        "fra_og_med": "2020-04-22",
                        "til_og_med": "2020-04-28"
                      }
                    ],
                    "utenlandsopphold_neste_12_mnd": [
                      
                    ]
                  },
                  "fra_og_med": "2020-04-01",
                  "til_og_med": "2020-04-10",
                  "vedlegg": [
                    "$vedleggUrl1"
                  ],
                  "har_medsoker": true,
                  "har_bekreftet_opplysninger": true,
                  "har_forstatt_rettigheter_og_plikter": true,
                  "ferieuttak_i_perioden": {
                    "skal_ta_ut_ferie_i_periode": true,
                    "ferieuttak": [
                      {
                        "fra_og_med": "2020-04-06",
                        "til_og_med": "2020-04-09"
                      }
                    ]
                  },
                  "har_hatt_inntekt_som_frilanser": true,
                  "frilans": {
                    "startdato": "2020-04-02",
                    "jobber_fortsatt_som_frilans": false
                  },
                  "har_hatt_inntekt_som_selvstendig_naringsdrivende": false,
                  "samtidig_hjemme": true,
                  "tilsynsordning": {
                    "svar": "ja",
                    "ja": {
                      "mandag": "PT5H0M",
                      "tirsdag": "PT5H0M",
                      "onsdag": "PT5H0M",
                      "torsdag": "PT5H0M",
                      "fredag": "PT5H0M",
                      "tilleggsinformasjon": "Forklaring for omsorgstilbud"
                    }
                  },
                  "nattevaak": {
                    "har_nattevaak": true,
                    "tilleggsinformasjon": "Må gjøre masse ved nattevåk"
                  },
                  "beredskap": {
                    "i_beredskap": true,
                    "tilleggsinformasjon": "Må være tilgjengelig ved beredskap blabla"
                  }
                }
            """.trimIndent()
        }

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
