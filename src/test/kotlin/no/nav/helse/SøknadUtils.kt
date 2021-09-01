package no.nav.helse

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.soknad.*
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.util.*


class SøknadUtils {
    companion object {
        fun forLangtNavn() =
            "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

        internal val objectMapper = jacksonObjectMapper().pleiepengesøknadKonfigurert()

        val søker = no.nav.helse.soker.Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fornavn = "Kjell",
            fødselsnummer = "26104500284"
        )

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
                        "fødselsnummer": "$fodselsnummer",
                        "navn": "Barn Barnesen"
                    },
                    "fraOgMed": "$fraOgMed",
                    "tilOgMed": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbeProsent": 50,
                                "skalJobbe": "redusert",
                                "arbeidsform": "FAST"
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
                                    "perioderBarnetErInnlagt" : [
                                      {
                                        "fraOgMed" : "2020-01-01",
                                        "tilOgMed": "2020-01-02"
                                      }
                                    ],
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
                  "beskrivelseOmsorgsrollen": "En kort beskrivelse",
                  "harVærtEllerErVernepliktig" : true
                }
                """.trimIndent()
        }

        fun bodyMedAktoerIdPaaBarn(
            aktørId: String,
            vedleggUrl1: String,
            vedleggUrl2: String
        ): String {
            //language=JSON
            return """
                {
                    "barn": {
                        "aktørId": "$aktørId",
                        "fødselsdato": "2021-01-01"
                    },
                    "fraOgMed": "2018-10-10",
                    "tilOgMed": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbe": "nei",
                                "arbeidsform": "FAST"
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
                  },
                "harVærtEllerErVernepliktig" : true
                }
                """.trimIndent()
        }

        fun bodyMedSelvstendigVirksomheterSomListe(
            vedleggUrl1: String,
            virksomheter: List<Virksomhet>,
            selvstendigArbeidsForhold: Arbeidsforhold
        ): String {
            val mapper = jacksonObjectMapper().dusseldorfConfigured()
                .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .writerWithDefaultPrettyPrinter()
            val virksomheterSomJson = mapper.writeValueAsString(virksomheter)
            val selvstendigArbeidsForholdSomJson = mapper.writeValueAsString(selvstendigArbeidsForhold)
            //language=JSON
            return """
                {
                    "barn": {
                      "fødselsnummer" : "02119970078"
                    },
                    "fraOgMed": "2018-10-10",
                    "tilOgMed": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbe": "nei",
                                "arbeidsform": "FAST"
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
                    "selvstendigVirksomheter" : $virksomheterSomJson,
                    "selvstendigArbeidsforhold": $selvstendigArbeidsForholdSomJson,
                    "harVærtEllerErVernepliktig" : true
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
                        "fødselsnummer": "$fodselsnummer",
                        "navn": "Barn Barnesen"
                    },
                    "fraOgMed": "$fraOgMed",
                    "tilOgMed": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ",
                                "skalJobbeProsent": $skalJobbeProsent,
                                "skalJobbe": "$skalJobbe",
                                "arbeidsform": "FAST"
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
                                    "perioderBarnetErInnlagt" : [
                                      {
                                        "fraOgMed" : "2020-01-01",
                                        "tilOgMed": "2020-01-02"
                                      }
                                    ],
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
                  "beskrivelseOmsorgsrollen": "En kort beskrivelse",
                  "harVærtEllerErVernepliktig" : true
                }
                """.trimIndent()
        }

        fun defaultSøknad(søknadId: String = UUID.randomUUID().toString()) = Søknad(
            newVersion = null,
            søknadId = søknadId,
            språk = Språk.nb,
            barn = BarnDetaljer(
                fødselsnummer = "03028104560",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen",
                aktørId = null
            ),
            barnRelasjon = BarnRelasjon.ANNET,
            barnRelasjonBeskrivelse = "Gudfar til barnet",
            arbeidsgivere = ArbeidsgiverDetaljer(
                listOf(
                    OrganisasjonDetaljer(
                        navn = "Org",
                        organisasjonsnummer = "917755736",
                        skalJobbeProsent = 40.0,
                        jobberNormaltTimer = 40.0,
                        skalJobbe = SkalJobbe.REDUSERT,
                        arbeidsform = Arbeidsform.FAST
                    )
                )
            ),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-01-20"),
            nattevåk = no.nav.helse.soknad.Nattevåk(
                harNattevåk = true,
                tilleggsinformasjon = "Har nattevåk"
            ),
            selvstendigVirksomheter = listOf(
                Virksomhet(
                    næringstyper = listOf(Næringstyper.ANNEN),
                    fiskerErPåBladB = false,
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    næringsinntekt = 1111,
                    navnPåVirksomheten = "TullOgTøys",
                    registrertINorge = false,
                    registrertIUtlandet = Land(
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    ),
                    varigEndring = VarigEndring(
                        inntektEtterEndring = 9999,
                        dato = LocalDate.parse("2020-01-01"),
                        forklaring = "Korona"
                    ),
                    regnskapsfører = Regnskapsfører(
                        "Kjell Regnskap",
                        "123456789"
                    ),
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.parse("2018-01-01")),
                    harFlereAktiveVirksomheter = true
                )
            ),
            selvstendigArbeidsforhold = Arbeidsforhold(
                skalJobbe = SkalJobbe.NEI,
                arbeidsform = Arbeidsform.FAST,
                jobberNormaltTimer = 40.0,
                skalJobbeProsent = 0.0
            ),
            skalPassePåBarnetIHelePerioden = true,
            omsorgstilbudV2 = OmsorgstilbudV2(
                planlagt = PlanlagtOmsorgstilbud(
                    ukedager = OmsorgstilbudUkedager(
                        mandag = Duration.ofHours(1),
                        tirsdag = Duration.ofHours(1),
                        onsdag = Duration.ofHours(1),
                        torsdag = Duration.ofHours(1),
                        fredag = Duration.ofHours(1)
                    ),
                    vetOmsorgstilbud = VetOmsorgstilbud.VET_ALLE_TIMER
                )
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2018-01-01"),
                        tilOgMed = LocalDate.parse("2018-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )
                ),
                utenlandsoppholdSiste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2017-01-01"),
                        tilOgMed = LocalDate.parse("2017-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )
                )
            ),
            harMedsøker = true,
            beredskap = no.nav.helse.soknad.Beredskap(
                beredskap = true,
                tilleggsinformasjon = "Ikke beredskap"
            ),
            beskrivelseOmsorgsrollen = "En kort beskrivelse",
            samtidigHjemme = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            skalBekrefteOmsorg = true,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = true, opphold = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-02")
                            )
                        ),
                        erUtenforEøs = false,
                        årsak = Årsak.ANNET
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-02")
                            )
                        ),
                        erUtenforEøs = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-02")
                            )
                        ),
                        erUtenforEøs = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = false,
                        erUtenforEøs = false,
                        årsak = null
                    )
                )
            ),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2020-01-05"),
                        tilOgMed = LocalDate.parse("2020-01-07")
                    )
                )
            ),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01"),
                arbeidsforhold = Arbeidsforhold(
                    skalJobbe = SkalJobbe.NEI,
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
                    skalJobbeProsent = 0.0
                )
            ),
            harVærtEllerErVernepliktig = true
        )
    }
}
