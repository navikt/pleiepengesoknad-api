package no.nav.helse

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.soknad.*
import no.nav.k9.søknad.felles.LovbestemtFerie
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.aktivitet.*
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.*
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning
import java.math.BigDecimal
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class SøknadUtils {
    companion object {
        fun forLangtNavn() =
            "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

        internal val objectMapper = jacksonObjectMapper().pleiepengesøknadKonfigurert()

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
                        "aktørId": "$aktørId",
                        "fødselsnummer" : "26104500284"
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
                    "barn": {
                        "fødselsnummer" : "26104500284"
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
                    "barn": {
                        "fødselsdato" : "2021-01-01"
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
                    "barn": {
                        "fødselsnummer" : "26104500284"
                    },
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

        fun defaultSøknad(søknadId: String = UUID.randomUUID().toString()) = Søknad(
            newVersion = null,
            søknadId = søknadId,
            språk = Språk.nb,
            barn = BarnDetaljer(
                fødselsnummer = "123456789",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen",
                aktørId = null
            ),
            arbeidsgivere = ArbeidsgiverDetaljer(
                listOf(
                    OrganisasjonDetaljer(
                        navn = "Org",
                        organisasjonsnummer = "917755736",
                        skalJobbeProsent = 40.0,
                        jobberNormaltTimer = 40.0,
                        skalJobbe = "redusert"
                    )
                )
            ),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-01-20"),
            bekrefterPeriodeOver8Uker = true,
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
                skalTaUtFerieIPerioden = false, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2020-01-05"),
                        tilOgMed = LocalDate.parse("2020-01-07")
                    )
                )
            ),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01")
            )
        )

        fun defaultK9FormatPSB(søknadId: UUID = UUID.randomUUID()) = K9Søknad(
            SøknadId.of(søknadId.toString()),
            Versjon.of("1.0"),
            ZonedDateTime.parse("2020-01-01T10:00:00Z"),
            Søker.builder()
                .norskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                .build(),
            PleiepengerSyktBarn(
                Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")),
                SøknadInfo(
                    "Far",
                    true,
                    "beskriver omsorgsrollen...",
                    true,
                    true,
                    true,
                    true,
                    true,
                    true
                ),
                Barn(
                    NorskIdentitetsnummer.of("10987654321"),
                    null
                ),
                ArbeidAktivitet.builder()
                    .frilanser(Frilanser(LocalDate.parse("2020-01-01"), true))
                    .selvstendigNæringsdrivende(
                        listOf(
                            SelvstendigNæringsdrivende(
                                mapOf(
                                    Periode(
                                        LocalDate.parse("2018-01-01"),
                                        LocalDate.parse("2020-01-01")
                                    ) to SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.builder()
                                        .erNyoppstartet(true)
                                        .registrertIUtlandet(false)
                                        .bruttoInntekt(BigDecimal(5_000_000))
                                        .erVarigEndring(true)
                                        .endringDato(LocalDate.parse("2020-01-01"))
                                        .endringBegrunnelse("Grunnet Covid-19")
                                        .landkode(Landkode.NORGE)
                                        .regnskapsførerNavn("Regnskapsfører Svensen")
                                        .regnskapsførerTelefon("+4799887766")
                                        .virksomhetstyper(listOf(VirksomhetType.DAGMAMMA, VirksomhetType.ANNEN))
                                        .build()
                                ),
                                Organisasjonsnummer.of("12345678910112233444455667"),
                                "Mamsen Bamsen AS"
                            ),
                            SelvstendigNæringsdrivende(
                                mapOf(
                                    Periode(
                                        LocalDate.parse("2015-01-01"),
                                        LocalDate.parse("2017-01-01")
                                    ) to SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.builder()
                                        .erNyoppstartet(false)
                                        .registrertIUtlandet(true)
                                        .bruttoInntekt(BigDecimal(500_000))
                                        .erVarigEndring(false)
                                        .endringDato(null)
                                        .endringBegrunnelse(null)
                                        .landkode(Landkode.SPANIA)
                                        .regnskapsførerNavn(null)
                                        .regnskapsførerTelefon(null)
                                        .virksomhetstyper(listOf(VirksomhetType.FISKE))
                                        .build()
                                ),
                                Organisasjonsnummer.of("54549049090490498048940940"),
                                "Something Fishy AS"
                            ),
                        )
                    )
                    .build(),
                Beredskap(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Beredskap.BeredskapPeriodeInfo("Jeg skal være i beredskap. Basta!"),
                        Periode(
                            LocalDate.parse("2020-01-07"),
                            LocalDate.parse("2020-01-10")
                        ) to Beredskap.BeredskapPeriodeInfo("Jeg skal være i beredskap i denne perioden også. Basta!")
                    )
                ),
                Nattevåk(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Nattevåk.NattevåkPeriodeInfo("Jeg skal ha nattevåk. Basta!"),
                        Periode(
                            LocalDate.parse("2020-01-07"),
                            LocalDate.parse("2020-01-10")
                        ) to Nattevåk.NattevåkPeriodeInfo("Jeg skal ha nattevåk i perioden også. Basta!")
                    )
                ),
                Tilsynsordning(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to TilsynPeriodeInfo(Duration.ofHours(8)),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to TilsynPeriodeInfo(Duration.ofHours(4))
                    )
                ),
                Arbeidstid(
                    listOf(
                        Arbeidstaker(
                            NorskIdentitetsnummer.of("12345678910"),
                            Organisasjonsnummer.of("926032925"),
                            ArbeidstidInfo(
                                Duration.ofHours(8),
                                mapOf(
                                    Periode(
                                        LocalDate.parse("2018-01-01"),
                                        LocalDate.parse("2020-01-05")
                                    ) to ArbeidstidPeriodeInfo(Duration.ofHours(4)),
                                    Periode(
                                        LocalDate.parse("2020-01-06"),
                                        LocalDate.parse("2020-01-10")
                                    ) to ArbeidstidPeriodeInfo(Duration.ofHours(2))
                                )
                            )
                        )
                    ),
                    null, null
                ),
                Uttak(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to UttakPeriodeInfo(Duration.ofHours(4)),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to UttakPeriodeInfo(Duration.ofHours(2))
                    )
                ),
                LovbestemtFerie(
                    listOf(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-05")),
                        Periode(LocalDate.parse("2020-01-06"), LocalDate.parse("2020-01-10"))
                    )
                ),
                Bosteder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Bosteder.BostedPeriodeInfo.builder()
                            .land(Landkode.SPANIA)
                            .build(),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to Bosteder.BostedPeriodeInfo.builder()
                            .land(Landkode.NORGE)
                            .build()
                    )
                ),
                Utenlandsopphold(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Utenlandsopphold.UtenlandsoppholdPeriodeInfo.builder()
                            .land(Landkode.CANADA)
                            .årsak(Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD)
                            .build(),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to Utenlandsopphold.UtenlandsoppholdPeriodeInfo.builder()
                            .land(Landkode.SVERIGE)
                            .årsak(Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING)
                            .build()
                    )
                )
            )
        )
    }
}
