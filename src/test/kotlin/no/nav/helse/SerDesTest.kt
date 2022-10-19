package no.nav.helse

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.Arbeidsgiver
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.Beredskap
import no.nav.helse.soknad.Bosted
import no.nav.helse.soknad.Ferieuttak
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.KomplettSøknad
import no.nav.helse.soknad.Land
import no.nav.helse.soknad.Medlemskap
import no.nav.helse.soknad.Nattevåk
import no.nav.helse.soknad.Periode
import no.nav.helse.soknad.Regnskapsfører
import no.nav.helse.soknad.SelvstendigNæringsdrivende
import no.nav.helse.soknad.Språk
import no.nav.helse.soknad.Søknad
import no.nav.helse.soknad.Utenlandsopphold
import no.nav.helse.soknad.UtenlandsoppholdIPerioden
import no.nav.helse.soknad.VarigEndring
import no.nav.helse.soknad.Virksomhet
import no.nav.helse.soknad.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.helse.soknad.domene.Frilans
import no.nav.helse.soknad.domene.Næringstyper
import no.nav.helse.soknad.domene.OpptjeningIUtlandet
import no.nav.helse.soknad.domene.OpptjeningType
import no.nav.helse.soknad.domene.UtenlandskNæring
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import no.nav.helse.soknad.Årsak
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    fun `Test reserialisering av request`() {
        val søknadId = UUID.randomUUID().toString()
        val søknad = SøknadUtils.defaultSøknad(søknadId)
        val søknadJson = søknadJson(søknadId)
        JSONAssert.assertEquals(søknadJson, søknad.somJson(), true)
        assertEquals(søknad, SøknadUtils.objectMapper.readValue(søknadJson))
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        val søknadId = UUID.randomUUID().toString()
        val komplettSøknad = komplettSøknad(søknadId)
        val komplettSøknadJson = komplettSøknadJson(søknadId)

        JSONAssert.assertEquals(komplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(komplettSøknad, SøknadUtils.objectMapper.readValue(komplettSøknadJson))
    }

    private companion object {
        fun søknadJson(søknadsId: String) =
            //language=json
            """
            {
              "newVersion": null,
              "søknadId" : "$søknadsId",
              "mottatt" : "2021-01-10T03:04:05.000000006Z",
              "språk": "nb",
              "samtidigHjemme": true,
              "barn": {
                "fødselsnummer": "03028104560",
                "navn": "Barn Barnesen",
                "fødselsdato" : "2018-01-01",
                "aktørId" : null,
                "årsakManglerIdentitetsnummer": null
              },
              "fraOgMed": "2021-01-01",
              "tilOgMed": "2021-10-01",
              "arbeidsgivere" :  [
                {
                  "navn": "Org",
                  "organisasjonsnummer": "917755736",
                  "erAnsatt": true,
                  "sluttetFørSøknadsperiode": null,
                  "arbeidsforhold": {
                  "normalarbeidstid": {
                    "erLiktHverUke": true,
                    "timerPerUkeISnitt": "PT37H30M",
                    "timerFasteDager": null
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "arbeiderIPerioden": "SOM_VANLIG",
                    "erLiktHverUke": null,
                    "fasteDager": null,
                    "prosentAvNormalt": null,
                    "timerPerUke": null,
                    "enkeltdager": null
                  }
                }
                },
                {
                  "navn": "JobberIkkeHerLenger",
                  "organisasjonsnummer" : "977155436",
                  "erAnsatt": false,
                  "sluttetFørSøknadsperiode": false,
                  "arbeidsforhold" : null
                }
              ],
              "vedlegg": [
                "http://localhost:8080/vedlegg/1"
              ],
              "opplastetIdVedleggId": [
                "http://localhost:8080/vedlegg/2"
              ],
              "medlemskap": {
                "harBoddIUtlandetSiste12Mnd": true,
                "skalBoIUtlandetNeste12Mnd": true,
                "utenlandsoppholdNeste12Mnd": [
                  {
                    "fraOgMed": "2018-01-01",
                    "tilOgMed": "2018-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ],
                "utenlandsoppholdSiste12Mnd": [
                  {
                    "fraOgMed": "2017-01-01",
                    "tilOgMed": "2017-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ]
              },
              "selvstendigNæringsdrivende": {
                "harInntektSomSelvstendig": true,
                "virksomhet": {
                  "næringstype":"ANNEN",
                  "fiskerErPåBladB": false,
                  "fraOgMed": "2020-01-01",
                  "tilOgMed": null,
                  "næringsinntekt": 1111,
                  "navnPåVirksomheten": "TullOgTøys",
                  "organisasjonsnummer": null,
                  "registrertINorge": false,
                  "registrertIUtlandet": {
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  },
                  "yrkesaktivSisteTreFerdigliknedeÅrene": {
                    "oppstartsdato": "2018-01-01"
                  },
                  "varigEndring": {
                    "dato": "2020-01-01",
                    "inntektEtterEndring": 9999,
                    "forklaring": "Korona"
                  },
                  "regnskapsfører": {
                    "navn": "Kjell Regnskap",
                    "telefon": "123456789"
                  },
                  "harFlereAktiveVirksomheter": true
                },
                "arbeidsforhold": {
                  "normalarbeidstid": {
                    "erLiktHverUke": true,
                    "timerPerUkeISnitt": "PT37H30M",
                    "timerFasteDager": null
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "arbeiderIPerioden": "SOM_VANLIG",
                    "erLiktHverUke": null,
                    "fasteDager": null,
                    "prosentAvNormalt": null,
                    "timerPerUke": null,
                    "enkeltdager": null
                  }
                }
              },
              "utenlandsoppholdIPerioden": {
                "skalOppholdeSegIUtlandetIPerioden": true,
                "opphold": [
                  {
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "landkode": "SE",
                    "landnavn": "Sverige",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2019-10-15",
                        "tilOgMed": "2019-10-20"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2020-11-10",
                    "tilOgMed": "2020-11-15",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-11-10",
                        "tilOgMed": "2020-11-12"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                  },{
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2022-12-10",
                    "tilOgMed": "2022-12-20",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": false,
                    "perioderBarnetErInnlagt" : [],
                    "årsak": null
                  }
                ]
              },
              "opptjeningIUtlandet": [
                {
                  "navn": "Kiwi AS",
                  "opptjeningType": "ARBEIDSTAKER",
                  "land": {
                    "landkode": "BEL",
                    "landnavn": "Belgia"
                  },
                  "fraOgMed": "2022-01-01",
                  "tilOgMed": "2022-01-10"
                }
              ],
              "utenlandskNæring" : [],
              "harMedsøker": true,
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ferieuttakIPerioden": {
                "skalTaUtFerieIPerioden": true,
                "ferieuttak": [
                  {
                    "fraOgMed": "2021-01-01",
                    "tilOgMed": "2021-01-10"
                  }
                ]
              },
              "beredskap": {
                "beredskap": true,
                "tilleggsinformasjon": "Ikke beredskap"
              },
              "frilans": {
                "startdato": "2018-01-01",
                "sluttdato": null,
                "jobberFortsattSomFrilans": true,
                "harInntektSomFrilanser": true,
                "arbeidsforhold": {
                  "normalarbeidstid": {
                    "erLiktHverUke": true,
                    "timerPerUkeISnitt": "PT37H30M",
                    "timerFasteDager": null
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "arbeiderIPerioden": "SOM_VANLIG",
                    "erLiktHverUke": null,
                    "fasteDager": null,
                    "prosentAvNormalt": null,
                    "timerPerUke": null,
                    "enkeltdager": null
                  }
                }
              },
              "nattevåk": {
                "harNattevåk": true,
                "tilleggsinformasjon": "Har nattevåk"
              },
              "omsorgstilbud": {
                "svarFortid": "JA",
                "svarFremtid": null,
                "erLiktHverUke": false,
                "ukedager" : null,
                "enkeltdager" : [
                      {
                        "dato": "2022-01-01",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-01-02",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-01-03",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-01-04",
                        "tid": "PT4H"
                      }
                    ]
               },
              "barnRelasjon" : "ANNET",
              "barnRelasjonBeskrivelse" : "Gudfar til barnet",
              "harVærtEllerErVernepliktig" : true
            }
        """.trimIndent()

        fun komplettSøknadJson(søknadsId: String) =
            //language=json
            """
        {
              "mottatt": "2020-05-05T00:00:00Z",
              "språk": "nb",
              "søknadId" : "$søknadsId",
              "søker": {
                "aktørId": "12345",
                "fødselsnummer": "26104500284",
                "fødselsdato": "1945-10-26",
                "etternavn": "Nordmann",
                "fornavn": "Ola",
                "mellomnavn": null,
                "myndig": true
              },
              "samtidigHjemme": true,
              "barn": {
                "fødselsnummer": "03028104560",
                "navn": "Barn Barnesen",
                "aktørId": "12345",
                "fødselsdato" : "2018-01-01",
                "årsakManglerIdentitetsnummer" : null
              },
              "fraOgMed": "2020-01-01",
              "tilOgMed": "2020-02-01",
              "arbeidsgivere": [
                {
                  "navn": "Org",
                  "organisasjonsnummer": "917755736",
                  "erAnsatt": true,
                  "sluttetFørSøknadsperiode" : null,
                  "arbeidsforhold": {
                      "normalarbeidstid": {
                        "erLiktHverUke": true,
                        "timerPerUkeISnitt": "PT37H30M",
                        "timerFasteDager": null
                      },
                      "arbeidIPeriode": {
                        "type": "ARBEIDER_VANLIG",
                        "arbeiderIPerioden": "SOM_VANLIG",
                        "erLiktHverUke": null,
                        "fasteDager": null,
                        "prosentAvNormalt": null,
                        "timerPerUke": null,
                        "enkeltdager": null
                      }
                    }
                }
              ],
              "vedleggId": [],
              "opplastetIdVedleggId": [],
              "medlemskap": {
                "harBoddIUtlandetSiste12Mnd": true,
                "skalBoIUtlandetNeste12Mnd": true,
                "utenlandsoppholdNeste12Mnd": [
                  {
                    "fraOgMed": "2018-01-01",
                    "tilOgMed": "2018-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ],
                "utenlandsoppholdSiste12Mnd": [
                  {
                    "fraOgMed": "2017-01-01",
                    "tilOgMed": "2017-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ]
              },
              "selvstendigNæringsdrivende": {
                "harInntektSomSelvstendig": true,
                "virksomhet": {
                  "næringstype": "ANNEN",
                  "fiskerErPåBladB": false,
                  "fraOgMed": "2020-01-01",
                  "tilOgMed": null,
                  "næringsinntekt": 1111,
                  "navnPåVirksomheten": "TullOgTøys",
                  "organisasjonsnummer": null,
                  "registrertINorge": false,
                  "registrertIUtlandet": {
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  },
                  "yrkesaktivSisteTreFerdigliknedeÅrene": {
                    "oppstartsdato": "2018-01-01"
                  },
                  "varigEndring": {
                    "dato": "2020-01-01",
                    "inntektEtterEndring": 9999,
                    "forklaring": "Korona"
                  },
                  "regnskapsfører": {
                    "navn": "Kjell Regnskap",
                    "telefon": "123456789"
                  },
                  "harFlereAktiveVirksomheter": true
                },
                "arbeidsforhold": {
                  "normalarbeidstid": {
                    "erLiktHverUke": true,
                    "timerPerUkeISnitt": "PT37H30M",
                    "timerFasteDager": null
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "arbeiderIPerioden": "SOM_VANLIG",
                    "erLiktHverUke": null,
                    "fasteDager": null,
                    "prosentAvNormalt": null,
                    "timerPerUke": null,
                    "enkeltdager": null
                  }
                }
              },
              "utenlandsoppholdIPerioden": {
                "skalOppholdeSegIUtlandetIPerioden": true,
                "opphold": [
                  {
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "landkode": "SE",
                    "landnavn": "Sverige",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-01-01",
                        "tilOgMed": "2020-01-02"
                      }
                    ],
                    "årsak": "ANNET"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-01-01",
                        "tilOgMed": "2020-01-02"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-01-01",
                        "tilOgMed": "2020-01-02"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
                  },{
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": false,
                    "perioderBarnetErInnlagt" : [],
                    "årsak": null
                  }
                ]
              },
              "opptjeningIUtlandet": [
                {
                  "navn": "Kiwi AS",
                  "opptjeningType": "ARBEIDSTAKER",
                  "land": {
                    "landkode": "BEL",
                    "landnavn": "Belgia"
                  },
                  "fraOgMed": "2022-01-01",
                  "tilOgMed": "2022-01-10"
                }
              ],
              "harMedsøker": true,
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ferieuttakIPerioden": {
                "skalTaUtFerieIPerioden": false,
                "ferieuttak": [
                  {
                    "fraOgMed": "2020-01-05",
                    "tilOgMed": "2020-01-07"
                  }
                ]
              },
              "utenlandskNæring" : [
                {
                  "næringstype" : "JORDBRUK_SKOGBRUK",
                  "navnPåVirksomheten" : "Flush AS",
                  "land" : {
                    "landkode" : "NLD",
                    "landnavn" : "Nederland"
                  },
                  "organisasjonsnummer" : "123ABC",
                  "fraOgMed" : "2022-01-05",
                  "tilOgMed" : null
                }
              ],
              "beredskap": {
                "beredskap": true,
                "tilleggsinformasjon": "Ikke beredskap"
              },
              "frilans": {
                  "jobberFortsattSomFrilans": true,
                  "harInntektSomFrilanser": true,
                  "startdato": "2018-01-01",
                  "sluttdato": null,
                  "arbeidsforhold": {
                    "normalarbeidstid": {
                      "erLiktHverUke": true,
                      "timerPerUkeISnitt": "PT37H30M",
                      "timerFasteDager": null
                    },
                    "arbeidIPeriode": {
                      "type": "ARBEIDER_VANLIG",
                      "arbeiderIPerioden": "SOM_VANLIG",
                      "erLiktHverUke": null,
                      "fasteDager": null,
                      "prosentAvNormalt": null,
                      "timerPerUke": null,
                      "enkeltdager": null
                    }
                  }
                },
              "nattevåk": {
                "harNattevåk": true,
                "tilleggsinformasjon": "Har nattevåk"
              },
              "omsorgstilbud": null,
              "barnRelasjon" : null,
              "barnRelasjonBeskrivelse" : null,
              "harVærtEllerErVernepliktig" : true,
              "k9FormatSøknad" : null 
            } 
        """.trimIndent()

        fun komplettSøknad(søknadId: String = UUID.randomUUID().toString()) = KomplettSøknad(
            mottatt = LocalDate.parse("2020-05-05").atStartOfDay(ZoneId.of("UTC")),
            språk = Språk.nb,
            søknadId = "$søknadId",
            barn = BarnDetaljer(
                aktørId = "12345",
                fødselsnummer = "03028104560",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen"
            ),
            søker = Søker(
                aktørId = "12345",
                fødselsnummer = "26104500284",
                fødselsdato = LocalDate.parse("1945-10-26"),
                etternavn = "Nordmann",
                fornavn = "Ola"
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold = no.nav.helse.soknad.domene.arbeid.Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            erLiktHverUke = true,
                            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                        ),
                        arbeidIPeriode = no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                            arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                        )
                    )
                )
            ),
            vedleggId = listOf(),
            opplastetIdVedleggId = listOf(),
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-02-01"),
            nattevåk = Nattevåk(
                harNattevåk = true,
                tilleggsinformasjon = "Har nattevåk"
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                harInntektSomSelvstendig = true,
                virksomhet = Virksomhet(
                    næringstype = Næringstyper.ANNEN,
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
                ),
                arbeidsforhold = no.nav.helse.soknad.domene.arbeid.Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        erLiktHverUke = true,
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                        arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                    )
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
            beredskap = Beredskap(
                beredskap = true,
                tilleggsinformasjon = "Ikke beredskap"
            ),
            samtidigHjemme = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            opptjeningIUtlandet = listOf(
                OpptjeningIUtlandet(
                    navn = "Kiwi AS",
                    opptjeningType = OpptjeningType.ARBEIDSTAKER,
                    land = Land(
                        landkode = "BEL",
                        landnavn = "Belgia",
                    ),
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10")
                )
            ),
            utenlandskNæring = listOf(
                UtenlandskNæring(
                    næringstype = Næringstyper.JORDBRUK_SKOGBRUK,
                    navnPåVirksomheten = "Flush AS",
                    land = Land("NLD", "Nederland"),
                    organisasjonsnummer = "123ABC",
                    fraOgMed = LocalDate.parse("2022-01-05")
                )
            ),
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
                skalTaUtFerieIPerioden = false, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2020-01-05"),
                        tilOgMed = LocalDate.parse("2020-01-07")
                    )
                )
            ),
            frilans = Frilans(
                harInntektSomFrilanser = true,
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01"),
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        erLiktHverUke = true,
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                        arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                    )
                )
            ),
            harVærtEllerErVernepliktig = true,
            k9FormatSøknad = null
        )
    }
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSøknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
