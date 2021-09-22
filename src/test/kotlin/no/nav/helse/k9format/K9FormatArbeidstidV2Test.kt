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

class K9FormatArbeidstidV2Test {

    @Test
    fun `Søknadsperiode med kun historisk på 3 dager hvor kun en historisk-enkeltdag er oppgitt -- Forventer at 2 dager blir fylt med 0 timer`() {
        val arbeidsgiverDetaljer = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 37.5,
                    skalJobbe = SkalJobbe.REDUSERT,
                    arbeidsform = Arbeidsform.FAST,
                    planlagt = null,
                    historisk = ArbeidIPeriode(
                        jobber = true,
                        jobberRedustert = true,
                        erLiktHverUke = false,
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
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = arbeidsgiverDetaljer,
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null
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

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid").getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Søknadsperiode med kun historisk på 3 dager hvor jobber=true, jobberRedusert=false -- Forventer periode med 100%`() {
        val arbeidsgiverDetaljer = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 37.5,
                    skalJobbe = SkalJobbe.REDUSERT,
                    arbeidsform = Arbeidsform.FAST,
                    planlagt = null,
                    historisk = ArbeidIPeriode(
                        jobber = true,
                        jobberRedustert = false,
                        erLiktHverUke = false,
                        enkeltdager = null,
                        fasteDager = null
                    )
                )
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = arbeidsgiverDetaljer,
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null
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

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid").getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Søknadsperiode med kun historisk på 3 dager hvor jobber=false -- Forventer periode med 0%`() {
        val arbeidsgiverDetaljer = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 37.5,
                    skalJobbe = SkalJobbe.REDUSERT,
                    arbeidsform = Arbeidsform.FAST,
                    planlagt = null,
                    historisk = ArbeidIPeriode(
                        jobber = false,
                        jobberRedustert = false,
                        erLiktHverUke = false,
                        enkeltdager = null,
                        fasteDager = null
                    )
                )
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = arbeidsgiverDetaljer,
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null
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

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid").getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Søknadsperiode med kun planlagt på 3 dager hvor jobber=false -- Forventer periode med 0%`() {
        val arbeidsgiverDetaljer = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 37.5,
                    skalJobbe = SkalJobbe.REDUSERT,
                    arbeidsform = Arbeidsform.FAST,
                    historisk = null,
                    planlagt = ArbeidIPeriode(
                        jobber = false,
                        jobberRedustert = false,
                        erLiktHverUke = false,
                        enkeltdager = listOf(),
                        fasteDager = null
                    )
                )
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = arbeidsgiverDetaljer,
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null
        )

        val k9Format = søknad.tilK9Format(ZonedDateTime.of(2021, 1, 1, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker, LocalDate.parse("2021-01-01"))
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

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid").getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        println(json)
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Søknadsperiode med kun planlagt på 3 dager hvor kun en planlagt-enkeltdag er oppgitt -- Forventer at 2 dager blir fylt med 0 timer`() {
        val arbeidsgiverDetaljer = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 37.5,
                    skalJobbe = SkalJobbe.REDUSERT,
                    arbeidsform = Arbeidsform.FAST,
                    planlagt = ArbeidIPeriode(
                        jobber = true,
                        jobberRedustert = true,
                        erLiktHverUke = false,
                        enkeltdager = listOf(
                            Enkeltdag(
                                dato = LocalDate.parse("2021-01-04"),
                                tid = Duration.ofHours(5)
                            )
                        ),
                        fasteDager = null
                    ),
                    historisk = null
                )
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"),
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = arbeidsgiverDetaljer,
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null
        )

        val k9Format = søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker, LocalDate.parse("2021-01-01"))
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

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid").getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }

    @Test
    fun `Søknadsperiode med kun planlagt på 3 dager med ukeplan -- Forventer at de dagene blir brukt`(){
        val arbeidsgiverDetaljer = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 37.5,
                    skalJobbe = SkalJobbe.REDUSERT,
                    arbeidsform = Arbeidsform.FAST,
                    planlagt = ArbeidIPeriode(
                        jobber = true,
                        jobberRedustert = true,
                        erLiktHverUke = false,
                        enkeltdager = null,
                        fasteDager = PlanUkedager(
                            mandag = Duration.ofHours(3),
                            tirsdag = Duration.ofHours(4),
                            onsdag = Duration.ofHours(5),
                            torsdag = Duration.ofHours(6),
                            fredag = Duration.ofHours(6)
                        )
                    ),
                    historisk = null
                )
            )
        )

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.parse("2021-01-04"), //Altså mandag, tirsdag, onsdag
            tilOgMed = LocalDate.parse("2021-01-06"),
            arbeidsgivere = arbeidsgiverDetaljer,
            omsorgstilbudV2 = null,
            utenlandsoppholdIPerioden = null,
            ferieuttakIPerioden = null
        )

        val k9Format = søknad.tilK9Format(ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")), SøknadUtils.søker, LocalDate.parse("2021-01-01"))
        søknad.validate(k9Format)

        val forventetJson = """
            {
              "perioder": {
                "2021-01-04/2021-01-04": {
                  "faktiskArbeidTimerPerDag": "PT3H",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-06/2021-01-06": {
                  "faktiskArbeidTimerPerDag": "PT5H",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                },
                "2021-01-05/2021-01-05": {
                  "faktiskArbeidTimerPerDag": "PT4H",
                  "jobberNormaltTimerPerDag": "PT7H30M"
                }
              }
            }
        """.trimIndent()

        val json = JSONObject(k9Format.somJson()).getJSONObject("ytelse").getJSONObject("arbeidstid").getJSONArray("arbeidstakerList").getJSONObject(0).getJSONObject("arbeidstidInfo")
        println(json)
        JSONAssert.assertEquals(JSONObject(forventetJson), json, true)
    }
}