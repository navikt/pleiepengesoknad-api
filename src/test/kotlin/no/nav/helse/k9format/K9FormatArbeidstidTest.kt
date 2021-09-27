package no.nav.helse.k9format

import no.nav.helse.SøknadUtils
import no.nav.helse.soknad.*
import no.nav.helse.somJson
import no.nav.k9.søknad.felles.type.Periode
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

class K9FormatArbeidstidTest {

    @Test
    fun `ansatt - Kun historisk med jobberIPerioden=JA jobberSomVanlig=false hvor det er oppgitt en enkeltdag -- Forventer at resten av perioden blir fylt med 0 timer`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                erAktivtArbeidsforhold = true,
                planlagt = null,
                historisk = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = false,
                    enkeltdager = listOf(
                        Enkeltdag(
                            LocalDate.parse("2021-01-04"),
                            tid = Duration.ofHours(5)
                        )
                    ),
                    fasteDager = null
                )
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format =
            søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
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
    fun `ansatt - Kun historisk med jobberIPerioden=JA jobberSomVanlig=true -- Forventer at perioden blir fylt hvor faktiskArbeidTimerPerDag=jobberNormaltTimer`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                planlagt = null,
                historisk = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = true,
                    enkeltdager = null,
                    fasteDager = null
                ),
                erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format =
            søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
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
    fun `ansatt - Kun historisk med jobberIPerioden=NEI -- Forventer at perioden blir fylt med 0 faktiskArbeidTimerPerDag`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                planlagt = null,
                historisk = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.NEI,
                    jobberSomVanlig = true,
                    enkeltdager = null,
                    fasteDager = null
                ), erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format =
            søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
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
    fun `ansatt - Kun historisk med jobberIPerioden=VET_IKKE -- Forventer at perioden blir fylt med 0 faktiskArbeidTimerPerDag`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                planlagt = null,
                historisk = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.VET_IKKE,
                    jobberSomVanlig = true,
                    enkeltdager = null,
                    fasteDager = null
                ), erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format =
            søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker)
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
    fun `ansatt - Kun planlagt med jobberIPerioden=JA jobberSomVanlig=false hvor det er oppgitt en enkeltdag -- Forventer at resten av perioden blir fylt med 0 timer`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                historisk = null,
                planlagt = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = false,
                    enkeltdager = listOf(
                        Enkeltdag(
                            dato = LocalDate.parse("2021-01-04"),
                            tid = Duration.ofHours(5)
                        )
                    ),
                    fasteDager = null
                ), erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker,
            LocalDate.parse("2021-01-01")
        )
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
    fun `ansatt - Kun planlagt med jobberIPerioden=NEI -- Forventer at perioden blir fylt med 0 faktiskArbeidTimerPerDag`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                historisk = null,
                planlagt = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.NEI,
                    jobberSomVanlig = false,
                    enkeltdager = null,
                    fasteDager = null
                ), erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 1, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker,
            LocalDate.parse("2021-01-01")
        )
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
        println(json)
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `ansatt - Kun planlagt med jobberIPerioden=JA med fasteDager oppgitt -- Forventer at de dagene blir brukt`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                historisk = null,
                planlagt = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = false,
                    enkeltdager = null,
                    fasteDager = PlanUkedager(
                        mandag = Duration.ofHours(2),
                        tirsdag = Duration.ofHours(3),
                        onsdag = Duration.ofHours(4),
                        torsdag = Duration.ofHours(5),
                        fredag = Duration.ofHours(6)
                    )
                ), erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"), //Altså mandag, tirsdag, onsdag
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker,
            LocalDate.parse("2021-01-01")
        )
        søknad.validate(k9Format)

        val forventetJson = """
            {
              "perioder": {
                "2021-01-04/2021-01-04": {
                  "faktiskArbeidTimerPerDag": "PT2H",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-06/2021-01-06": {
                  "faktiskArbeidTimerPerDag": "PT4H",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-05/2021-01-05": {
                  "faktiskArbeidTimerPerDag": "PT3H",
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
    fun `ansatt - Kun planlagt hvor jobberIPerioden=JA jobberSomVanlig=true  -- Forventer perioden fylt med faktiskArbeidTimerPerDag=jobberNormaltTimer`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                historisk = null,
                planlagt = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = true,
                    enkeltdager = null,
                    fasteDager = null
                ), erAktivtArbeidsforhold = true
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"), //Altså mandag, tirsdag, onsdag
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker,
            LocalDate.parse("2021-01-01")
        )
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
    fun `ansatt - Søknad med historisk og planlagt hvor jobberSomVanlig=true -- Forventer to perioder med full arbeid`() {
        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 37.5,
                arbeidsform = Arbeidsform.FAST,
                planlagt = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = true,
                    enkeltdager = null,
                    fasteDager = null
                ),
                historisk = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.JA,
                    jobberSomVanlig = true,
                    enkeltdager = null,
                    fasteDager = null
                ),
                erAktivtArbeidsforhold = true
            )
        )


        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = null,
            selvstendigNæringsdrivende = null
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker,
            LocalDate.parse("2021-01-05")
        )
        søknad.validate(k9Format)

        val forventetJson = """
            {
              "perioder": {
                "2021-01-04/2021-01-04": {
                  "faktiskArbeidTimerPerDag": "PT7H30M",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-05/2021-01-06": {
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
    fun `Arbeidsforhold - Historisk og planlagt hvor jobberSomVanlig=true -- Forventer to perioder med fult arbeid`(){
        val arbeidsforholdJson = Arbeidsforhold(
            arbeidsform = Arbeidsform.FAST,
            jobberNormaltTimer = 37.5,
            erAktivtArbeidsforhold = true,
            historisk = ArbeidIPeriode(
                jobberIPerioden = JobberIPeriodeSvar.JA,
                jobberSomVanlig = true,
                enkeltdager = null,
                fasteDager = null
            ),
            planlagt = ArbeidIPeriode(
                jobberIPerioden = JobberIPeriodeSvar.JA,
                jobberSomVanlig = true,
                enkeltdager = null,
                fasteDager = null
            )
        ).tilK9ArbeidstidInfo(Periode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-10")), LocalDate.parse("2021-01-05")).somJson()

        //language=json
        val forventetJson = """
            {
              "perioder": {
                "2021-01-01/2021-01-04": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT7H30M"
                },
                "2021-01-05/2021-01-10": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT7H30M"
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventetJson), JSONObject(arbeidsforholdJson), true)
    }

    @Test
    fun `Arbeidsforhold - Historisk og planlagt hvor jobberSomVanlig=false -- Forventer alle ukedager i perioden fylt med 0 timer`(){
        val arbeidsforholdJson = Arbeidsforhold(
            arbeidsform = Arbeidsform.FAST,
            jobberNormaltTimer = 37.5,
            erAktivtArbeidsforhold = true,
            historisk = ArbeidIPeriode(
                jobberIPerioden = JobberIPeriodeSvar.JA,
                jobberSomVanlig = false,
                enkeltdager = null,
                fasteDager = null
            ),
            planlagt = ArbeidIPeriode(
                jobberIPerioden = JobberIPeriodeSvar.JA,
                jobberSomVanlig = false,
                enkeltdager = listOf(),
                fasteDager = null
            )
        ).tilK9ArbeidstidInfo(Periode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-10")), LocalDate.parse("2021-01-05")).somJson()

        //language=json
        val forventetJson = """
            {
              "perioder": {
                "2021-01-01/2021-01-01": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2021-01-04/2021-01-04": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2021-01-05/2021-01-05": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2021-01-06/2021-01-06": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2021-01-07/2021-01-07": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2021-01-08/2021-01-08": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                }
              }
            }
        """.trimIndent()
        println(arbeidsforholdJson)
        JSONAssert.assertEquals(JSONObject(forventetJson), JSONObject(arbeidsforholdJson), true)
    }

    @Test
    fun `Kombinasjon av selvstending, ansatt og frilans -- Forventer to perioder per ytelse med full arbeid`(){
        val arbeidIPeriodenUtenOppgittTid = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.JA,
            jobberSomVanlig = true,
            enkeltdager = null,
            fasteDager = null
        )

        val arbeidsforhold = Arbeidsforhold(
            jobberNormaltTimer = 37.5,
            arbeidsform = Arbeidsform.FAST,
            erAktivtArbeidsforhold = true,
            planlagt = arbeidIPeriodenUtenOppgittTid,
            historisk = arbeidIPeriodenUtenOppgittTid
        )

        val arbeidsforholdAnsatt = ArbeidsforholdAnsatt(
            navn = "Org",
            organisasjonsnummer = "917755736",
            arbeidsforhold = arbeidsforhold
        )

        val frilans = Frilans(
            startdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
            arbeidsforhold = arbeidsforhold
        )

        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            virksomhet = Virksomhet(
                næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                fiskerErPåBladB = false,
                fraOgMed = LocalDate.parse("2019-01-01"),
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = true,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                harFlereAktiveVirksomheter = true
            ),
            arbeidsforhold = arbeidsforhold
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"), //5 dager
            tilOgMed = LocalDate.parse("2021-01-08"),
            ansatt = listOf(arbeidsforholdAnsatt),
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null,
            frilans = frilans,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
        )

        val k9Format = søknad.tilK9Format(
            ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            SøknadUtils.søker,
            LocalDate.parse("2021-01-06")
        )
        søknad.validate(k9Format)

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid")

        val forventetJson = """
            {
              "frilanserArbeidstidInfo": {
                "perioder": {
                  "2021-01-06/2021-01-08": {
                    "faktiskArbeidTimerPerDag": "PT7H30M",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-04/2021-01-05": {
                    "faktiskArbeidTimerPerDag": "PT7H30M",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  }
                }
              },
              "arbeidstakerList": [
                {
                  "arbeidstidInfo": {
                    "perioder": {
                      "2021-01-06/2021-01-08": {
                        "faktiskArbeidTimerPerDag": "PT7H30M",
                        "jobberNormaltTimerPerDag": "PT7H30M"
                      },
                      "2021-01-04/2021-01-05": {
                        "faktiskArbeidTimerPerDag": "PT7H30M",
                        "jobberNormaltTimerPerDag": "PT7H30M"
                      }
                    }
                  },
                  "organisasjonsnummer": "917755736",
                  "norskIdentitetsnummer": null
                }
              ],
              "selvstendigNæringsdrivendeArbeidstidInfo": {
                "perioder": {
                  "2021-01-06/2021-01-08": {
                    "faktiskArbeidTimerPerDag": "PT7H30M",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  },
                  "2021-01-04/2021-01-05": {
                    "faktiskArbeidTimerPerDag": "PT7H30M",
                    "jobberNormaltTimerPerDag": "PT7H30M"
                  }
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }
}