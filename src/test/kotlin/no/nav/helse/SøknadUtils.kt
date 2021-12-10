package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.soknad.ArbeidIPeriode
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.ArbeidsforholdAnsatt
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.BarnRelasjon
import no.nav.helse.soknad.Beredskap
import no.nav.helse.soknad.Bosted
import no.nav.helse.soknad.Ferieuttak
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.JobberIPeriodeSvar
import no.nav.helse.soknad.Land
import no.nav.helse.soknad.Medlemskap
import no.nav.helse.soknad.Nattevåk
import no.nav.helse.soknad.Næringstyper
import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.Periode
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.PlanlagtOmsorgstilbud
import no.nav.helse.soknad.Regnskapsfører
import no.nav.helse.soknad.SelvstendigNæringsdrivende
import no.nav.helse.soknad.Språk
import no.nav.helse.soknad.Søknad
import no.nav.helse.soknad.Utenlandsopphold
import no.nav.helse.soknad.UtenlandsoppholdIPerioden
import no.nav.helse.soknad.VarigEndring
import no.nav.helse.soknad.Virksomhet
import no.nav.helse.soknad.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.helse.soknad.Årsak
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
            arbeidsgivere = listOf(
                ArbeidsforholdAnsatt(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold = Arbeidsforhold(
                        jobberNormaltTimer = 40.0,
                        historiskArbeid = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.JA,
                            erLiktHverUke = true,
                            enkeltdager = null,
                            fasteDager = PlanUkedager(
                                mandag = Duration.ofHours(7).plusMinutes(30)
                            )
                        ),
                        planlagtArbeid = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.JA,
                            erLiktHverUke = true,
                            enkeltdager = null,
                            fasteDager = PlanUkedager(
                                mandag = Duration.ofHours(7).plusMinutes(30)
                            )
                        )
                    )
                ),
                ArbeidsforholdAnsatt(
                    navn = "JobberIkkeHerLenger",
                    organisasjonsnummer = "977155436",
                    erAnsatt = false,
                    sluttetFørSøknadsperiode = false
                )
            ),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            fraOgMed = LocalDate.parse("2021-01-01"),
            tilOgMed = LocalDate.parse("2021-10-01"),
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
                    jobberNormaltTimer = 40.0,
                    historiskArbeid = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.JA,
                        erLiktHverUke = true,
                        enkeltdager = null,
                        fasteDager = PlanUkedager(
                            mandag = Duration.ofHours(7).plusMinutes(30)
                        )
                    ),
                    planlagtArbeid = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.JA,
                        erLiktHverUke = true,
                        enkeltdager = null,
                        fasteDager = PlanUkedager(
                            mandag = Duration.ofHours(7).plusMinutes(30)
                        )
                    )
                )
            ),
            omsorgstilbud = Omsorgstilbud(
                planlagt = PlanlagtOmsorgstilbud(
                    ukedager = PlanUkedager(
                        mandag = Duration.ofHours(1),
                        tirsdag = Duration.ofHours(1),
                        onsdag = Duration.ofHours(1),
                        torsdag = Duration.ofHours(1),
                        fredag = Duration.ofHours(1)
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
                        fraOgMed = LocalDate.parse("2021-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-10")
                    )
                )
            ),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                startdato = LocalDate.parse("2018-01-01"),
                arbeidsforhold = Arbeidsforhold(
                    jobberNormaltTimer = 40.0,
                    historiskArbeid = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.JA,
                        erLiktHverUke = true,
                        enkeltdager = null,
                        fasteDager = PlanUkedager(
                            mandag = Duration.ofHours(7).plusMinutes(30)
                        )
                    ),
                    planlagtArbeid = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.JA,
                        erLiktHverUke = true,
                        enkeltdager = null,
                        fasteDager = PlanUkedager(
                            mandag = Duration.ofHours(7).plusMinutes(30)
                        )
                    )
                )
            ),
            harVærtEllerErVernepliktig = true
        )
    }
}
