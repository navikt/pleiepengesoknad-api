package no.nav.helse

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.*
import no.nav.helse.vedlegg.Vedlegg
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    fun `Test reserialisering av request`(){
        JSONAssert.assertEquals(SøknadJson, søknad.somJson(), true)
        assertEquals(
            søknad, SoknadUtils.objectMapper.readValue(SøknadJson)
        )
   }

    @Test
    fun `Test serialisering av request til mottak`() {
        JSONAssert.assertEquals(KomplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(komplettSøknad, SoknadUtils.objectMapper.readValue(KomplettSøknadJson))
    }

    private companion object {
        val now = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        internal val start = LocalDate.parse("2020-01-01")

        internal val søknad = Søknad(
            newVersion = null,
            språk = Språk.nb,
            barn = BarnDetaljer(
                aktørId = "12345",
                fødselsnummer = "123456789",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen"
            ),
            relasjonTilBarnet = "mor",
            arbeidsgivere = ArbeidsgiverDetaljer(listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 10.0,
                    skalJobbe = "redusert"
                )
            )),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(10),
            bekrefterPeriodeOver8Uker = true,
            nattevåk = Nattevåk(
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
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.parse("2018-01-01"))
                )
            ),
            skalPassePåBarnetIHelePerioden = true,
            tilsynsordning = Tilsynsordning(
                svar = TilsynsordningSvar.ja,
                ja = TilsynsordningJa(
                    mandag = Duration.ofHours(1),
                    tirsdag = Duration.ofHours(1),
                    onsdag = Duration.ofHours(1),
                    torsdag = Duration.ofHours(1),
                    fredag = Duration.ofHours(1),
                    tilleggsinformasjon = "Blabla"
                ),
                vetIkke = TilsynsordningVetIkke(
                    svar = TilsynsordningVetIkkeSvar.annet,
                    annet = "Nei"
                )
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2018-01-01"),
                        tilOgMed =  LocalDate.parse("2018-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                )),
                utenlandsoppholdSiste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2017-01-01"),
                        tilOgMed =  LocalDate.parse("2017-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    ))
            ),
            harMedsøker = true,
            beredskap = Beredskap(
                beredskap = true,
                tilleggsinformasjon = "Ikke beredskap"
            ),
            beskrivelseOmsorgsrollen = "En kort beskrivelse",
            samtidigHjemme = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            skalBekrefteOmsorg = true,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = true, opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2019-10-10"),
                    tilOgMed = LocalDate.parse("2019-11-10"),
                    landkode = "SE",
                    landnavn = "Sverige",
                    erBarnetInnlagt = true,
                    erUtenforEøs = false,
                    årsak = Årsak.ANNET
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2019-10-10"),
                    tilOgMed = LocalDate.parse("2019-11-10"),
                    landkode = "SE",
                    landnavn = "Sverige",
                    erBarnetInnlagt = true,
                    erUtenforEøs = false,
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2019-10-10"),
                    tilOgMed = LocalDate.parse("2019-11-10"),
                    landkode = "SE",
                    landnavn = "Sverige",
                    erBarnetInnlagt = true,
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
            )),
            ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf(
                Ferieuttak(
                    fraOgMed = LocalDate.parse("2020-01-05"),
                    tilOgMed = LocalDate.parse("2020-01-07")
                )
            )),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01")
            )
        )



        internal val SøknadJson = """
            {
              "newVersion": null,
              "språk": "nb",
              "samtidigHjemme": true,
              "barn": {
                "fødselsnummer": "123456789",
                "navn": "Barn Barnesen",
                "aktørId": "12345",
                "fødselsdato" : "2018-01-01"
              },
              "relasjonTilBarnet": "mor",
              "fraOgMed": "${LocalDate.now()}",
              "tilOgMed": "${LocalDate.now().plusDays(10)}",
              "arbeidsgivere": {
                "organisasjoner": [
                  {
                    "organisasjonsnummer": "917755736",
                    "navn": "Org",
                    "skalJobbeProsent": 10,
                    "jobberNormaltTimer": 10,
                    "skalJobbe": "redusert",
                    "vetIkkeEkstrainfo": null
                  }
                ]
              },
              "vedlegg": [
                "http://localhost:8080/vedlegg/1"
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
              "selvstendigVirksomheter": [
                {
                    "næringstyper": ["ANNEN"],
                    "fiskerErPåBladB": false,
                    "organisasjonsnummer": null,
                    "fraOgMed": "2020-01-01",
                    "tilOgMed": null,
                    "næringsinntekt": 1111,
                    "navnPåVirksomheten": "TullOgTøys",
                    "registrertINorge": false,
                    "registrertIUtlandet":{
                      "landnavn": "Tyskland",
                      "landkode": "DEU" 
                    },
                    "varigEndring": {
                      "inntektEtterEndring": 9999,
                      "dato": "2020-01-01",
                      "forklaring": "Korona"
                    },
                    "regnskapsfører": {
                      "navn": "Kjell Regnskap",
                      "telefon": "123456789"
                    },
                    "revisor": null,
                    "yrkesaktivSisteTreFerdigliknedeÅrene": {
                        "oppstartsdato": "2018-01-01"
                        }
                }
              ],
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
                    "årsak": "ANNET"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
                  },{
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": false,
                    "årsak": null
                  }
                ]
              },
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
              "skalBekrefteOmsorg": true,
              "skalPassePåBarnetIHelePerioden": true,
              "beskrivelseOmsorgsrollen": "En kort beskrivelse",
              "bekrefterPeriodeOver8Uker": true,
              "beredskap": {
                "beredskap": true,
                "tilleggsinformasjon": "Ikke beredskap"
              },
              "frilans": {
              "jobberFortsattSomFrilans": true,
              "startdato": "2018-01-01"
              },
              "nattevåk": {
                "harNattevåk": true,
                "tilleggsinformasjon": "Har nattevåk"
              },
              "tilsynsordning": {
                "svar": "ja",
                "ja": {
                  "mandag": "PT1H",
                  "tirsdag": "PT1H",
                  "onsdag": "PT1H",
                  "torsdag": "PT1H",
                  "fredag": "PT1H",
                  "tilleggsinformasjon": "Blabla"
                },
                "vetIkke": {
                  "svar": "annet",
                  "annet": "Nei"
                }
              }
            }
        """.trimIndent()

        internal val KomplettSøknadJson = """
        {
              "mottatt": "2020-05-05T00:00:00Z",
              "språk": "nb",
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
                "fødselsnummer": "123456789",
                "navn": "Barn Barnesen",
                "aktørId": "12345",
                "fødselsdato" : "2018-01-01"
              },
              "relasjonTilBarnet": "mor",
              "fraOgMed": "${LocalDate.now()}",
              "tilOgMed": "${LocalDate.now().plusDays(10)}",
              "arbeidsgivere": {
                "organisasjoner": [
                  {
                    "organisasjonsnummer": "917755736",
                    "navn": "Org",
                    "skalJobbeProsent": 10,
                    "jobberNormaltTimer": 10,
                    "skalJobbe": "redusert",
                    "vetIkkeEkstrainfo": null
                  }
                ]
              },
              "vedlegg": [
                {
                "content": "VGVzdA==",
                "contentType": "image/png",
                "title": "Vedlegg"
                }
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
              "selvstendigVirksomheter": [
                {
                    "næringstyper": ["ANNEN"],
                    "fiskerErPåBladB": false,
                    "organisasjonsnummer": null,
                    "fraOgMed": "2020-01-01",
                    "tilOgMed": null,
                    "næringsinntekt": 1111,
                    "navnPåVirksomheten": "TullOgTøys",
                    "registrertINorge": false,
                    "registrertIUtlandet":{
                      "landnavn": "Tyskland",
                      "landkode": "DEU" 
                    },
                    "varigEndring": {
                      "inntektEtterEndring": 9999,
                      "dato": "2020-01-01",
                      "forklaring": "Korona"
                    },
                    "regnskapsfører": {
                      "navn": "Kjell Regnskap",
                      "telefon": "123456789"
                    },
                    "revisor": null,
                    "yrkesaktivSisteTreFerdigliknedeÅrene": {
                        "oppstartsdato": "2018-01-01"
                        }
                }
              ],
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
                    "årsak": "ANNET"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
                  },{
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": false,
                    "årsak": null
                  }
                ]
              },
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
              "skalBekrefteOmsorg": true,
              "skalPassePåBarnetIHelePerioden": true,
              "beskrivelseOmsorgsrollen": "En kort beskrivelse",
              "bekrefterPeriodeOver8Uker": true,
              "beredskap": {
                "beredskap": true,
                "tilleggsinformasjon": "Ikke beredskap"
              },
              "frilans": {
              "jobberFortsattSomFrilans": true,
              "startdato": "2018-01-01"
              },
              "nattevåk": {
                "harNattevåk": true,
                "tilleggsinformasjon": "Har nattevåk"
              },
              "tilsynsordning": {
                "svar": "ja",
                "ja": {
                  "mandag": "PT1H",
                  "tirsdag": "PT1H",
                  "onsdag": "PT1H",
                  "torsdag": "PT1H",
                  "fredag": "PT1H",
                  "tilleggsinformasjon": "Blabla"
                },
                "vetIkke": {
                  "svar": "annet",
                  "annet": "Nei"
                }
              }
            }
        """.trimIndent()

        internal val komplettSøknad = KomplettSøknad(
            mottatt = LocalDate.parse("2020-05-05").atStartOfDay(ZoneId.of("UTC")),
            språk = Språk.nb,
            barn = BarnDetaljer(
                aktørId = "12345",
                fødselsnummer = "123456789",
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
            relasjonTilBarnet = "mor",
            arbeidsgivere = ArbeidsgiverDetaljer(listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = 10.0,
                    jobberNormaltTimer = 10.0,
                    skalJobbe = "redusert"
                )
            )),
            vedlegg = listOf(
                Vedlegg(
                    content = "Test".toByteArray(),
                    contentType = "image/png",
                    title = "Vedlegg"
                )
            ),
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now().plusDays(10),
            bekrefterPeriodeOver8Uker = true,
            nattevåk = Nattevåk(
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
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.parse("2018-01-01"))
                )
            ),
            skalPassePåBarnetIHelePerioden = true,
            tilsynsordning = Tilsynsordning(
                svar = TilsynsordningSvar.ja,
                ja = TilsynsordningJa(
                    mandag = Duration.ofHours(1),
                    tirsdag = Duration.ofHours(1),
                    onsdag = Duration.ofHours(1),
                    torsdag = Duration.ofHours(1),
                    fredag = Duration.ofHours(1),
                    tilleggsinformasjon = "Blabla"
                ),
                vetIkke = TilsynsordningVetIkke(
                    svar = TilsynsordningVetIkkeSvar.annet,
                    annet = "Nei"
                )
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2018-01-01"),
                        tilOgMed =  LocalDate.parse("2018-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )),
                utenlandsoppholdSiste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2017-01-01"),
                        tilOgMed =  LocalDate.parse("2017-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    ))
            ),
            harMedsøker = true,
            beredskap = Beredskap(
                beredskap = true,
                tilleggsinformasjon = "Ikke beredskap"
            ),
            beskrivelseOmsorgsrollen = "En kort beskrivelse",
            samtidigHjemme = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            skalBekrefteOmsorg = true,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = true, opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2019-10-10"),
                    tilOgMed = LocalDate.parse("2019-11-10"),
                    landkode = "SE",
                    landnavn = "Sverige",
                    erBarnetInnlagt = true,
                    erUtenforEøs = false,
                    årsak = Årsak.ANNET
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2019-10-10"),
                    tilOgMed = LocalDate.parse("2019-11-10"),
                    landkode = "SE",
                    landnavn = "Sverige",
                    erBarnetInnlagt = true,
                    erUtenforEøs = false,
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2019-10-10"),
                    tilOgMed = LocalDate.parse("2019-11-10"),
                    landkode = "SE",
                    landnavn = "Sverige",
                    erBarnetInnlagt = true,
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
            )),
            ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf(
                Ferieuttak(
                    fraOgMed = LocalDate.parse("2020-01-05"),
                    tilOgMed = LocalDate.parse("2020-01-07")
                )
            )),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01")
            )
        )
    }
}

internal fun Søknad.somJson() = SoknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSøknad.somJson() = SoknadUtils.objectMapper.writeValueAsString(this)