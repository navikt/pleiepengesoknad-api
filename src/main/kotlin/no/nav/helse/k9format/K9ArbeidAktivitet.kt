package no.nav.helse.k9format

import no.nav.helse.soker.Søker
import no.nav.helse.soknad.*
import no.nav.k9.søknad.felles.aktivitet.*
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo

internal fun Søknad.byggK9ArbeidAktivitet(søker: Søker): ArbeidAktivitet {
    val builder = ArbeidAktivitet.builder()

    frilans?.let {
        builder.frilanser(frilans.tilK9Frilanser())
    }

    builder.selvstendigNæringsdrivende(selvstendigVirksomheter.tilK9SelvstendigNæringsdrivende())
    builder.arbeidstaker(arbeidsgivere.tilK9Arbeidstaker(søker.fødselsnummer, Periode(fraOgMed, tilOgMed)))

    return builder.build()
}

internal fun Frilans.tilK9Frilanser(): Frilanser = Frilanser.builder()
    .jobberFortsattSomFrilans(this.jobberFortsattSomFrilans)
    .startdato(this.startdato)
    .build()

internal fun ArbeidsgiverDetaljer.tilK9Arbeidstaker(
    identitetsnummer: String,
    periode: Periode
): List<Arbeidstaker> {
    return organisasjoner.map { organisasjon ->
        Arbeidstaker(
            NorskIdentitetsnummer.of(identitetsnummer),
            Organisasjonsnummer.of(organisasjon.organisasjonsnummer),
            organisasjon.tilK9ArbeidstidInfo(periode)
        )
    }
}

fun List<Virksomhet>.tilK9SelvstendigNæringsdrivende(): List<SelvstendigNæringsdrivende> = map { virksomhet ->
    SelvstendigNæringsdrivende.builder()
        .organisasjonsnummer(Organisasjonsnummer.of(virksomhet.organisasjonsnummer))
        .virksomhetNavn(virksomhet.navnPåVirksomheten)
        .periode(
            Periode(virksomhet.fraOgMed, virksomhet.tilOgMed),
            virksomhet.tilK9SelvstendingNæringsdrivendeInfo()
        )
        .build()
}

internal fun Virksomhet.tilK9SelvstendingNæringsdrivendeInfo(): SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo {
    val infoBuilder = SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.builder()
    infoBuilder
        .virksomhetstyper(næringstyper.tilK9VirksomhetType())

    if (registrertINorge) {
        infoBuilder
            .landkode(Landkode.NORGE)
            .registrertIUtlandet(false)
    } else {
        infoBuilder
            .landkode(Landkode.of(registrertIUtlandet!!.landkode))
            .registrertIUtlandet(true)
    }

    infoBuilder.erNyoppstartet(true) //TODO Må sjekke hva som er riktig her
    yrkesaktivSisteTreFerdigliknedeÅrene?.let {
        infoBuilder.erNyoppstartet(false)
    }

    regnskapsfører?.let {
        infoBuilder
            .regnskapsførerNavn(regnskapsfører.navn)
            .regnskapsførerTelefon(regnskapsfører.telefon)
    }

    næringsinntekt?.let {
        infoBuilder
            .bruttoInntekt(næringsinntekt.toBigDecimal())
    }

    infoBuilder.erVarigEndring(false)
    varigEndring?.let {
        infoBuilder
            .erVarigEndring(true)
            .endringDato(it.dato)
            .endringBegrunnelse(it.forklaring)
    }

    return infoBuilder.build()
}

internal fun List<Næringstyper>.tilK9VirksomhetType(): List<VirksomhetType> = map {
    when (it) {
        Næringstyper.FISKE -> VirksomhetType.FISKE
        Næringstyper.JORDBRUK_SKOGBRUK -> VirksomhetType.JORDBRUK_SKOGBRUK
        Næringstyper.DAGMAMMA -> VirksomhetType.DAGMAMMA
        Næringstyper.ANNEN -> VirksomhetType.ANNEN
    }
}

fun OrganisasjonDetaljer.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo {
    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    val faktiskTimerPerUke = jobberNormaltTimer.tilFaktiskTimerPerUke(skalJobbeProsent)
    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()
    val faktiskArbeidstimerPerDag = faktiskTimerPerUke.tilTimerPerDag().tilDuration()

    perioder[periode] = ArbeidstidPeriodeInfo(faktiskArbeidstimerPerDag)

    return ArbeidstidInfo(normalTimerPerDag, perioder)
}