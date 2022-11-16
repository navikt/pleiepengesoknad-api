package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.soknad.Arbeidsgiver
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.BarnRelasjon
import no.nav.helse.soknad.Beredskap
import no.nav.helse.soknad.Bosted
import no.nav.helse.soknad.Enkeltdag
import no.nav.helse.soknad.Ferieuttak
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.Land
import no.nav.helse.soknad.Medlemskap
import no.nav.helse.soknad.Nattevåk
import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.OmsorgstilbudSvarFortid
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
import no.nav.helse.soknad.domene.FrilanserOppdrag
import no.nav.helse.soknad.domene.FrilanserOppdragIPerioden
import no.nav.helse.soknad.domene.FrilanserOppdragType
import no.nav.helse.soknad.domene.FrilanserV2
import no.nav.helse.soknad.domene.Næringstyper
import no.nav.helse.soknad.domene.OpptjeningIUtlandet
import no.nav.helse.soknad.domene.OpptjeningType
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import no.nav.helse.soknad.Årsak
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class SøknadUtils {
    companion object {
        internal val objectMapper = jacksonObjectMapper().pleiepengesøknadKonfigurert()

        val søker = no.nav.helse.soker.Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fornavn = "Kjell",
            fødselsnummer = "26104500284"
        )

        fun defaultSøknad(søknadId: String = UUID.randomUUID().toString()) = Søknad(
            newVersion = null,
            apiDataVersjon = "1.0.0",
            søknadId = søknadId,
            mottatt = ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
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
                Arbeidsgiver(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
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
                Arbeidsgiver(
                    navn = "JobberIkkeHerLenger",
                    organisasjonsnummer = "977155436",
                    erAnsatt = false,
                    sluttetFørSøknadsperiode = false
                )
            ),
            vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
            fødselsattestVedleggUrls = listOf(URL("http://localhost:8080/vedlegg/2")),
            fraOgMed = LocalDate.parse("2021-01-01"),
            tilOgMed = LocalDate.parse("2021-10-01"),
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
            omsorgstilbud = Omsorgstilbud(
                svarFortid = OmsorgstilbudSvarFortid.JA,
                erLiktHverUke = false,
                enkeltdager = listOf(
                    Enkeltdag(
                        LocalDate.parse("2022-01-01"),
                        Duration.ofHours(4)
                    ),
                    Enkeltdag(
                        LocalDate.parse("2022-01-02"),
                        Duration.ofHours(4)
                    ),
                    Enkeltdag(
                        LocalDate.parse("2022-01-03"),
                        Duration.ofHours(4)
                    ),
                    Enkeltdag(
                        LocalDate.parse("2022-01-04"),
                        Duration.ofHours(4)
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
                                fraOgMed = LocalDate.parse("2019-10-15"),
                                tilOgMed = LocalDate.parse("2019-10-20")
                            )
                        ),
                        erUtenforEøs = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2020-11-10"),
                        tilOgMed = LocalDate.parse("2020-11-15"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-11-10"),
                                tilOgMed = LocalDate.parse("2020-11-12")
                            )
                        ),
                        erUtenforEøs = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2022-12-10"),
                        tilOgMed = LocalDate.parse("2022-12-20"),
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
                harInntektSomFrilanser = true,
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
            frilanserOppdrag = FrilanserV2(
                harInntektSomFrilanser = true,
                oppdrag = listOf(
                    FrilanserOppdrag(
                        navn = "Frilanser Hansen",
                        organisasjonsnummer = "12345678910",
                        offentligIdent = null,
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.FRILANSER,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = null,
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(8),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR,
                                prosentAvNormalt = 0.0
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "Frilanser Hansen Privat",
                        organisasjonsnummer = null,
                        offentligIdent = "01109324567",
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.FRILANSER,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = LocalDate.parse("2022-11-10"),
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(8),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR,
                                prosentAvNormalt = 0.0
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "Styremedlem Hansen",
                        organisasjonsnummer = "12345678910",
                        offentligIdent = null,
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.STYREMELEM_ELLER_VERV,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = null,
                        styremedlemHeleInntekt = false,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(8),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR,
                                prosentAvNormalt = 0.0
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "Styremedlem Hansen privat",
                        organisasjonsnummer = null,
                        offentligIdent = "01109324567",
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.STYREMELEM_ELLER_VERV,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = LocalDate.parse("2022-11-10"),
                        styremedlemHeleInntekt = true,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(8),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR,
                                prosentAvNormalt = 0.0
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "Fosterforelder Hansen",
                        organisasjonsnummer = null,
                        offentligIdent = "01109324567",
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.FOSTERFORELDER,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = null,
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = null
                    ),
                    FrilanserOppdrag(
                        navn = "Fosterforelder Hansen",
                        organisasjonsnummer = null,
                        offentligIdent = "01109324567",
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.FOSTERFORELDER,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = LocalDate.parse("2022-11-10"),
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = null
                    ),
                    FrilanserOppdrag(
                        navn = "NAV",
                        organisasjonsnummer = "12345678910",
                        offentligIdent = null,
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.OMSORGSSTØNAD,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = null,
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(40),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.HELT_FRAVÆR,
                                timerPerUke = Duration.ZERO
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "NAV",
                        organisasjonsnummer = "12345678910",
                        offentligIdent = null,
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.OMSORGSSTØNAD,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = LocalDate.parse("2022-11-10"),
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(40),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                                prosentAvNormalt = 50.0
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "Manuell oppføring",
                        organisasjonsnummer = null,
                        offentligIdent = null,
                        manuellOppføring = true,
                        oppdragType = FrilanserOppdragType.OMSORGSSTØNAD,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN,
                        ansattFom = LocalDate.parse("2022-11-08"),
                        ansattTom = LocalDate.parse("2022-11-10"),
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(40),
                            ), arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                                prosentAvNormalt = 50.0
                            )
                        )
                    ),
                    FrilanserOppdrag(
                        navn = "Har ikke oppdrag i perioden",
                        organisasjonsnummer = null,
                        offentligIdent = null,
                        manuellOppføring = false,
                        oppdragType = FrilanserOppdragType.FRILANSER,
                        harOppdragIPerioden = FrilanserOppdragIPerioden.NEI,
                        ansattFom = null,
                        ansattTom = null,
                        styremedlemHeleInntekt = null,
                        arbeidsforhold = null
                    )
                )
            ),
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
            utenlandskNæring = listOf(),
            harVærtEllerErVernepliktig = true
        )
    }
}
