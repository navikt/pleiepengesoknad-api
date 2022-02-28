package no.nav.helse.k9format

import no.nav.helse.SøknadUtils
import no.nav.helse.soknad.*
import no.nav.helse.somJson
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

class K9FormatArbeidstidTest {

    @Test
    fun `Dersom harFraværIPeriode er false bruker man jobberNormaltTimer for planlagt og faktisk timer`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = true,
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidIPeriode = null,
                harFraværIPeriode = false
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
        søknad.validate(k9Format)

        val forventetJson = """
            {
              "perioder": {
                "2021-01-04/2021-01-06": {
                  "faktiskArbeidTimerPerDag": "PT7H30M",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                }
              }
            }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
            .getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Dersom søker jobber i perioden og det er oppgitt en enkeltdag, forventer man at resten av perioden blir fylt med 0 timer`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = true,
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberProsent = 50.0,
                    erLiktHverUke = false,
                    enkeltdager = listOf(
                        Enkeltdag(
                            LocalDate.parse("2021-01-04"),
                            tid = Duration.ofHours(5)
                        )
                    ),
                    fasteDager = null
                ),
                harFraværIPeriode = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
        søknad.validate(k9Format)

        val forventetJson = """
            {
              "perioder": {
                "2021-01-04/2021-01-04": {
                  "faktiskArbeidTimerPerDag": "PT5H",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-06/2021-01-06": {
                  "faktiskArbeidTimerPerDag": "PT0S",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-05/2021-01-05": {
                  "faktiskArbeidTimerPerDag": "PT0S",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                }
              }
            }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
            .getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Dersom søker jobber og har oppgitt full plan, forventer man at perioden blir fylt`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = true,
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    erLiktHverUke = true,
                    enkeltdager = null,
                    fasteDager = PlanUkedager(
                        mandag = Duration.ofHours(3).plusMinutes(30),
                        tirsdag = Duration.ofHours(4).plusMinutes(30),
                        onsdag = Duration.ofHours(5).plusMinutes(30),
                        torsdag = Duration.ofHours(6).plusMinutes(30),
                        fredag = Duration.ofHours(7).plusMinutes(30)
                    )
                ),
                harFraværIPeriode = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format =
            søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
        søknad.validate(k9Format)

        //language=json
        val forventetJson = """
        {
          "perioder": {
            "2021-01-04/2021-01-04": {
              "faktiskArbeidTimerPerDag": "PT3H30M",
              "jobberNormaltTimerPerDag": "PT7H30M"
            },
            "2021-01-05/2021-01-05": {
              "faktiskArbeidTimerPerDag": "PT4H30M",
              "jobberNormaltTimerPerDag": "PT7H30M"
            },
            "2021-01-06/2021-01-06": {
              "faktiskArbeidTimerPerDag": "PT5H30M",
              "jobberNormaltTimerPerDag": "PT7H30M"
            }
          }
        }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
            .getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Dersom søker jobber og har oppgitt plan med noen hull, forventer man at perioden blir fylt og dagene som ikke er oppgitt får 0 timer`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = true,
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    erLiktHverUke = true,
                    enkeltdager = null,
                    fasteDager = PlanUkedager(
                        mandag = Duration.ofHours(3).plusMinutes(30),
                        tirsdag = null,
                        onsdag = Duration.ofHours(5).plusMinutes(30),
                        torsdag = null,
                        fredag = Duration.ofHours(7).plusMinutes(30)
                    )
                ),
                harFraværIPeriode = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-07"),
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format =
            søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
        søknad.validate(k9Format)

        //language=json
        val forventetJson = """
        {
          "perioder": {
            "2021-01-04/2021-01-04": {
              "faktiskArbeidTimerPerDag": "PT3H30M",
              "jobberNormaltTimerPerDag": "PT7H30M"
            },
            "2021-01-05/2021-01-05": {
              "faktiskArbeidTimerPerDag": "PT0S",
              "jobberNormaltTimerPerDag": "PT7H30M"
            },
            "2021-01-06/2021-01-06": {
              "faktiskArbeidTimerPerDag": "PT5H30M",
              "jobberNormaltTimerPerDag": "PT7H30M"
            },
            "2021-01-07/2021-01-07": {
              "faktiskArbeidTimerPerDag": "PT0S",
              "jobberNormaltTimerPerDag": "PT7H30M"
            }
          }
        }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
            .getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Dersom søker ikke jobber i perioden, forventer man at perioden blir fylt med 0 faktiskArbeidTimerPerDag`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = true,
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.NEI,
                    erLiktHverUke = null,
                    enkeltdager = null,
                    fasteDager = null
                ),
                harFraværIPeriode = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
        søknad.validate(k9Format)

        val forventetJson = """
        {
          "perioder": {
            "2021-01-04/2021-01-06": {
              "faktiskArbeidTimerPerDag": "PT0S",
              "jobberNormaltTimerPerDag": "PT7H30M"
            }
          }
        }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
            .getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Dersom søker har sluttet før perioden og arbeidsforhold=null, forventer man at perioden er fylt med 0-0 timer`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = false,
            arbeidsforhold = null
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker
        )
        søknad.validate(k9Format)

        val forventetJson = """
            {
              "perioder": {
                "2021-01-04/2021-01-06": {
                  "faktiskArbeidTimerPerDag": "PT0S",
                  "jobberNormaltTimerPerDag": "PT0S"
                }
              }
            }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
            .getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Kombinasjon av selvstending, ansatt og frilans med arbeidsforhold=null-- Forventer en periode per arbeid med 0-0 timer`() {
        val arbeidsforholdAnsatt = Arbeidsgiver(
            navn = "Org",
            organisasjonsnummer = "917755736",
            erAnsatt = false,
            arbeidsforhold = null
        )

        val frilans = Frilans(
            startdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
            arbeidsforhold = null
        )

        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            virksomhet = Virksomhet(
                næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                fiskerErPåBladB = false,
                fraOgMed = LocalDate.parse("2019-01-01"),
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = true,
                organisasjonsnummer = "926032925",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                harFlereAktiveVirksomheter = true
            ),
            arbeidsforhold = null
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"), //5 dager
            tilOgMed = LocalDate.parse("2021-01-08"),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            arbeidsgivere = listOf(arbeidsforholdAnsatt),
            frilans = frilans,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker
        )
        søknad.validate(k9Format)

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")
        println(json)
        val forventetJson = """
            {
              "frilanserArbeidstidInfo": {
                "perioder": {
                  "2021-01-04/2021-01-08": {
                    "faktiskArbeidTimerPerDag": "PT0S",
                    "jobberNormaltTimerPerDag": "PT0S"
                  }
                }
              },
              "arbeidstakerList": [
                {
                  "arbeidstidInfo": {
                    "perioder": {
                      "2021-01-04/2021-01-08": {
                        "faktiskArbeidTimerPerDag": "PT0S",
                        "jobberNormaltTimerPerDag": "PT0S"
                      }
                    }
                  },
                  "organisasjonsnummer": "917755736",
                  "norskIdentitetsnummer": null
                }
              ],
              "selvstendigNæringsdrivendeArbeidstidInfo": {
                "perioder": {
                  "2021-01-04/2021-01-08": {
                    "faktiskArbeidTimerPerDag": "PT0S",
                    "jobberNormaltTimerPerDag": "PT0S"
                  }
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Frilans hvor start og sluttdato er innenfor søknadsperioden -- Forventer at alt utenfor blir fylt med 0 timer`() {
        val arbeidIPeriode = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberProsent = 50.0,
            erLiktHverUke = true,
            enkeltdager = null,
            fasteDager = PlanUkedager(
                mandag = Duration.ofHours(3),
                tirsdag = Duration.ofHours(3),
                onsdag = Duration.ofHours(3),
                torsdag = Duration.ofHours(3),
                fredag = Duration.ofHours(3)
            )
        )

        val arbeidsforhold = Arbeidsforhold(
            jobberNormaltTimer = 37.5,
            arbeidIPeriode = arbeidIPeriode,
            harFraværIPeriode = true
        )

        val frilans = Frilans(
            startdato = LocalDate.parse("2021-01-06"),
            sluttdato = LocalDate.parse("2021-01-13"),
            jobberFortsattSomFrilans = false,
            arbeidsforhold = arbeidsforhold
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-15"),
            arbeidsgivere = listOf(),
            omsorgstilbud = null,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
            ferieuttakIPerioden = null,
            selvstendigNæringsdrivende = null,
            frilans = frilans
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker
        )
        søknad.validate(k9Format)

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")

        val forventetJson = """
            {
              "frilanserArbeidstidInfo": {
                "perioder": {
                  "2021-01-04/2021-01-04": {
                    "faktiskArbeidTimerPerDag": "PT0S",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-12/2021-01-12": {
                    "faktiskArbeidTimerPerDag": "PT3H",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-11/2021-01-11": {
                    "faktiskArbeidTimerPerDag": "PT3H",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-08/2021-01-08": {
                    "faktiskArbeidTimerPerDag": "PT3H",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-07/2021-01-07": {
                    "faktiskArbeidTimerPerDag": "PT3H",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-15/2021-01-15": {
                    "faktiskArbeidTimerPerDag": "PT0S",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-06/2021-01-06": {
                    "faktiskArbeidTimerPerDag": "PT3H",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-14/2021-01-14": {
                    "faktiskArbeidTimerPerDag": "PT0S",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-05/2021-01-05": {
                    "faktiskArbeidTimerPerDag": "PT0S",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-13/2021-01-13": {
                    "faktiskArbeidTimerPerDag": "PT3H",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  }
                }
              },
              "arbeidstakerList": [],
              "selvstendigNæringsdrivendeArbeidstidInfo": null
            }
        """.trimIndent()
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }
}
