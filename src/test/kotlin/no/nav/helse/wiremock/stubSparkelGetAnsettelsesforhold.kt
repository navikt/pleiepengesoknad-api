package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelGetAnsettelsesforhold() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/sparkel-mock/api/arbeidsforhold.*")) // .*
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(sparkelResponse)
            )
    )
}

private val sparkelResponse = """
{
  "arbeidsforhold": [
    {
      "permisjonOgPermittering": [],
      "endretAv": "Z991012",
      "antallTimerForTimeloennet": [],
      "arbeidsforholdInnrapportertEtterAOrdningen": true,
      "arbeidsforholdID": "1",
      "opplysningspliktig": {
        "orgnummer": "910970046"
      },
      "arbeidsavtale": [
        {
          "avtaltArbeidstimerPerUke": 40,
          "endretAv": "Z991012",
          "fomGyldighetsperiode": "2017-04-01T00:00:00.000+02:00",
          "arbeidstidsordning": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidstidsordninger",
            "kodeRef": "ikkeSkift",
            "value": "Ikke skift"
          },
          "fomBruksperiode": "2018-10-30+01:00",
          "opphavREF": "3",
          "yrke": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Yrker",
            "kodeRef": "3431101",
            "value": "ADMINISTRASJONSSEKRETÃ\u0086R"
          },
          "avloenningstype": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Avloenningstyper",
            "kodeRef": "fast",
            "value": "FastlÃ¸nn"
          },
          "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
          "beregnetAntallTimerPrUke": 40,
          "applikasjonsID": "AAREG",
          "stillingsprosent": 100
        }
      ],
      "opphavREF": "3",
      "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
      "arbeidsforholdIDnav": 45526756,
      "utenlandsopphold": [],
      "arbeidsgiver": {
        "orgnummer": "910831143",
        "navn": "Maxbo"
      },
      "arbeidsforholdstype": {
        "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidsforholdstyper",
        "kodeRef": "ordinaertArbeidsforhold",
        "value": "OrdinÃ¦rt arbeidsforhold"
      },
      "opprettelsestidspunkt": "2018-10-30T11:26:37.029+01:00",
      "arbeidstaker": {
        "ident": {
          "ident": "08088806280"
        }
      },
      "sistBekreftet": "2018-10-30T11:26:36.000+01:00",
      "ansettelsesPeriode": {
        "endretAv": "Z991012",
        "fomBruksperiode": "2018-10-30+01:00",
        "opphavREF": "3",
        "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
        "applikasjonsID": "AAREG",
        "periode": {
          "fom": "2017-04-01T00:00:00.000+02:00"
        }
      },
      "opprettetAv": "Z991012",
      "applikasjonsID": "AAREG"
    },
    {
      "permisjonOgPermittering": [],
      "endretAv": "Z991012",
      "antallTimerForTimeloennet": [],
      "arbeidsforholdInnrapportertEtterAOrdningen": true,
      "arbeidsforholdID": "1",
      "opplysningspliktig": {
        "orgnummer": "910970046"
      },
      "arbeidsavtale": [
        {
          "avtaltArbeidstimerPerUke": 40,
          "endretAv": "Z991012",
          "fomGyldighetsperiode": "2017-04-01T00:00:00.000+02:00",
          "arbeidstidsordning": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidstidsordninger",
            "kodeRef": "ikkeSkift",
            "value": "Ikke skift"
          },
          "fomBruksperiode": "2018-10-30+01:00",
          "opphavREF": "3",
          "yrke": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Yrker",
            "kodeRef": "3431101",
            "value": "ADMINISTRASJONSSEKRETÃ\u0086R"
          },
          "avloenningstype": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Avloenningstyper",
            "kodeRef": "fast",
            "value": "FastlÃ¸nn"
          },
          "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
          "beregnetAntallTimerPrUke": 40,
          "applikasjonsID": "AAREG",
          "stillingsprosent": 100
        }
      ],
      "opphavREF": "3",
      "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
      "arbeidsforholdIDnav": 45526756,
      "utenlandsopphold": [],
      "arbeidsgiver": {
        "orgnummer": "910831143",
        "navn": "Maxbo"
      },
      "arbeidsforholdstype": {
        "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidsforholdstyper",
        "kodeRef": "ordinaertArbeidsforhold",
        "value": "OrdinÃ¦rt arbeidsforhold"
      },
      "opprettelsestidspunkt": "2018-10-30T11:26:37.029+01:00",
      "arbeidstaker": {
        "ident": {
          "ident": "08088806280"
        }
      },
      "sistBekreftet": "2018-10-30T11:26:36.000+01:00",
      "ansettelsesPeriode": {
        "endretAv": "Z991012",
        "fomBruksperiode": "2018-10-30+01:00",
        "opphavREF": "3",
        "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
        "applikasjonsID": "AAREG",
        "periode": {
          "fom": "2017-04-01T00:00:00.000+02:00"
        }
      },
      "opprettetAv": "Z991012",
      "applikasjonsID": "AAREG"
    },
    {
      "permisjonOgPermittering": [],
      "endretAv": "Z991012",
      "antallTimerForTimeloennet": [],
      "arbeidsforholdInnrapportertEtterAOrdningen": true,
      "arbeidsforholdID": "147",
      "opplysningspliktig": {
        "orgnummer": "923609016"
      },
      "arbeidsavtale": [
        {
          "avtaltArbeidstimerPerUke": 40,
          "endretAv": "Z991012",
          "fomGyldighetsperiode": "2017-04-01T00:00:00.000+02:00",
          "arbeidstidsordning": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidstidsordninger",
            "kodeRef": "ikkeSkift",
            "value": "Ikke skift"
          },
          "fomBruksperiode": "2018-10-30+01:00",
          "opphavREF": "3",
          "yrke": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Yrker",
            "kodeRef": "3431101",
            "value": "ADMINISTRASJONSSEKRETÃ\u0086R"
          },
          "avloenningstype": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Avloenningstyper",
            "kodeRef": "fast",
            "value": "FastlÃ¸nn"
          },
          "endringstidspunkt": "2018-10-30T11:38:54.966+01:00",
          "beregnetAntallTimerPrUke": 40,
          "applikasjonsID": "AAREG",
          "stillingsprosent": 100
        }
      ],
      "opphavREF": "3",
      "endringstidspunkt": "2018-10-30T11:38:54.966+01:00",
      "arbeidsforholdIDnav": 45526864,
      "utenlandsopphold": [],
      "arbeidsgiver": {
        "orgnummer": "973861778",
        "navn": "Telenor"
      },
      "arbeidsforholdstype": {
        "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidsforholdstyper",
        "kodeRef": "ordinaertArbeidsforhold",
        "value": "OrdinÃ¦rt arbeidsforhold"
      },
      "opprettelsestidspunkt": "2018-10-30T11:38:54.966+01:00",
      "arbeidstaker": {
        "ident": {
          "ident": "08088806280"
        }
      },
      "sistBekreftet": "2018-10-30T11:38:54.000+01:00",
      "ansettelsesPeriode": {
        "endretAv": "Z991012",
        "fomBruksperiode": "2018-10-30+01:00",
        "opphavREF": "3",
        "endringstidspunkt": "2018-10-30T11:38:54.966+01:00",
        "applikasjonsID": "AAREG",
        "periode": {
          "fom": "2017-04-01T00:00:00.000+02:00"
        }
      },
      "opprettetAv": "Z991012",
      "applikasjonsID": "AAREG"
    },
    {
      "permisjonOgPermittering": [],
      "endretAv": "Z991012",
      "antallTimerForTimeloennet": [],
      "arbeidsforholdInnrapportertEtterAOrdningen": true,
      "arbeidsforholdID": "1",
      "opplysningspliktig": {
        "orgnummer": "910970046"
      },
      "arbeidsavtale": [
        {
          "avtaltArbeidstimerPerUke": 40,
          "endretAv": "Z991012",
          "fomGyldighetsperiode": "2017-04-01T00:00:00.000+02:00",
          "arbeidstidsordning": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidstidsordninger",
            "kodeRef": "ikkeSkift",
            "value": "Ikke skift"
          },
          "fomBruksperiode": "2018-10-30+01:00",
          "opphavREF": "3",
          "yrke": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Yrker",
            "kodeRef": "3431101",
            "value": "ADMINISTRASJONSSEKRETÃ\u0086R"
          },
          "avloenningstype": {
            "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Avloenningstyper",
            "kodeRef": "fast",
            "value": "FastlÃ¸nn"
          },
          "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
          "beregnetAntallTimerPrUke": 40,
          "applikasjonsID": "AAREG",
          "stillingsprosent": 100
        }
      ],
      "opphavREF": "3",
      "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
      "arbeidsforholdIDnav": 45526756,
      "utenlandsopphold": [],
      "arbeidsgiver": {
        "ident": {
          "ident": "08088806280"
        }
      },
      "arbeidsforholdstype": {
        "kodeverksRef": "http://nav.no/kodeverk/Kodeverk/Arbeidsforholdstyper",
        "kodeRef": "ordinaertArbeidsforhold",
        "value": "OrdinÃ¦rt arbeidsforhold"
      },
      "opprettelsestidspunkt": "2018-10-30T11:26:37.029+01:00",
      "arbeidstaker": {
        "ident": {
          "ident": "08088806280"
        }
      },
      "sistBekreftet": "2018-10-30T11:26:36.000+01:00",
      "ansettelsesPeriode": {
        "endretAv": "Z991012",
        "fomBruksperiode": "2018-10-30+01:00",
        "opphavREF": "3",
        "endringstidspunkt": "2018-10-30T11:26:37.029+01:00",
        "applikasjonsID": "AAREG",
        "periode": {
          "fom": "2017-04-01T00:00:00.000+02:00"
        }
      },
      "opprettetAv": "Z991012",
      "applikasjonsID": "AAREG"
    }
  ]
}
""".trimIndent()

// Er 4 entries i response fra Sparkel. Men 2 er samme organisasjon, og en er privatperson, dermed er det bare 2 i expected.
val expectedGetAnsettelsesforholdJson = """
    {
        "organisasjoner": [
            {
                "navn": "Telenor",
                "organisasjonsnummer": "973861778"
            },
            {
                "navn": "Maxbo",
                "organisasjonsnummer": "910831143"
            }
        ]
    }
""".trimIndent()
