package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
            ansatt = listOf(
                ArbeidsforholdAnsatt(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    arbeidsforhold = Arbeidsforhold(
                        arbeidsform = Arbeidsform.FAST,
                        jobberNormaltTimer = 40.0,
                        erAktivtArbeidsforhold = null,
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
                    )
                )
            ),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            fraOgMed = LocalDate.now().minusDays(5),
            tilOgMed = LocalDate.now().plusDays(5),
            nattevåk = Nattevåk(
                harNattevåk = true,
                tilleggsinformasjon = "Har nattevåk"
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                virksomhet = Virksomhet(
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
                ),
                arbeidsforhold = Arbeidsforhold(
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
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
                )
            ),
            skalPassePåBarnetIHelePerioden = true,
            omsorgstilbudV2 = OmsorgstilbudV2(
                planlagt = PlanlagtOmsorgstilbud(
                    ukedager = PlanUkedager(
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
            beredskap = Beredskap(
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
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1)
                    )
                )
            ),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01"),
                arbeidsforhold = Arbeidsforhold(
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
                    erAktivtArbeidsforhold = null,
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
                )
            ),
            harVærtEllerErVernepliktig = true
        )
    }
}
