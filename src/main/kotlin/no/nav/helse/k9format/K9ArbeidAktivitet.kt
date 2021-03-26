package no.nav.helse.k9format

import no.nav.helse.soknad.*
import no.nav.k9.søknad.felles.opptjening.*
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

fun Double.tilFaktiskTimerPerUke(prosent: Double) = this.times(prosent.div(100))
fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)
fun Double.tilDuration() = Duration.ofMinutes((this * 60).toLong())

internal fun Søknad.byggK9OpptjeningAktivitet() = OpptjeningAktivitet(
    null, // arbeidstaker er ikke nødvendig i opptjeningAktivitet for psb.
    selvstendigVirksomheter.tilK9SelvstendigNæringsdrivende(),
    frilans?.tilK9Frilanser()
)

internal fun Frilans.tilK9Frilanser(): Frilanser = Frilanser(startdato, null, jobberFortsattSomFrilans) // TODO: 26/03/2021 mangler sluttdato på Frilanser

internal fun ArbeidsgiverDetaljer.tilK9Arbeidstaker(
    periode: Periode
): List<Arbeidstaker> {
    return organisasjoner.map { organisasjon ->
        Arbeidstaker(
            null, //K9 format vil ikke ha både fnr og org nummer
            Organisasjonsnummer.of(organisasjon.organisasjonsnummer),
            organisasjon.tilK9ArbeidstidInfo(periode)
        )
    }
}

fun List<Virksomhet>.tilK9SelvstendigNæringsdrivende(): List<SelvstendigNæringsdrivende> = map { virksomhet ->
    SelvstendigNæringsdrivende(
        mapOf(Periode(virksomhet.fraOgMed, virksomhet.tilOgMed) to virksomhet.tilK9SelvstendingNæringsdrivendeInfo()),
        Organisasjonsnummer.of(virksomhet.organisasjonsnummer),
        virksomhet.navnPåVirksomheten
    )
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
            .landkode(Landkode.of(registrertIUtlandet?.landkode))
            .registrertIUtlandet(true)
    }

    when (erEldreEnn3År()) {
        true -> infoBuilder.erNyoppstartet(false)
        false -> infoBuilder.erNyoppstartet(true)
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

    varigEndring?.let {
        infoBuilder
            .erVarigEndring(true)
            .endringDato(it.dato)
            .endringBegrunnelse(it.forklaring)
    } ?: infoBuilder.erVarigEndring(false)

    return infoBuilder.build()
}

private fun Virksomhet.erEldreEnn3År() =
    fraOgMed.isBefore(LocalDate.now().minusYears(3)) || fraOgMed.isEqual(LocalDate.now().minusYears(3))


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

    perioder[periode] = ArbeidstidPeriodeInfo(normalTimerPerDag, faktiskArbeidstimerPerDag)

    return ArbeidstidInfo(perioder)
}
