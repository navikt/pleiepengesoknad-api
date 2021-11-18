package no.nav.helse.k9format

import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.Næringstyper
import no.nav.helse.soknad.Søknad
import no.nav.helse.soknad.Virksomhet
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.VirksomhetType
import java.time.Duration
import java.time.LocalDate

fun Double.tilFaktiskTimerPerUke(prosent: Double) = this.times(prosent.div(100))
fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)
fun Double.tilDuration() = Duration.ofMinutes((this * 60).toLong())

internal fun Søknad.byggK9OpptjeningAktivitet(): OpptjeningAktivitet {
    val opptjeningAktivitet = OpptjeningAktivitet()
    selvstendigNæringsdrivende?.let { opptjeningAktivitet.medSelvstendigNæringsdrivende(it.tilK9SelvstendigNæringsdrivende()) }
    frilans?.let { opptjeningAktivitet.medFrilanser(it.tilK9Frilanser()) }
    return opptjeningAktivitet
}

internal fun Frilans.tilK9Frilanser(): Frilanser {
    val frilanser = Frilanser()
    frilanser.medStartDato(startdato)
    sluttdato?.let { frilanser.medSluttDato(it) }
    return frilanser
}

fun no.nav.helse.soknad.SelvstendigNæringsdrivende.tilK9SelvstendigNæringsdrivende(): List<SelvstendigNæringsdrivende> {

    return listOf(
        SelvstendigNæringsdrivende(
            mapOf(
                Periode(
                    virksomhet.fraOgMed,
                    virksomhet.tilOgMed
                ) to virksomhet.tilK9SelvstendingNæringsdrivendeInfo()
            ),
            Organisasjonsnummer.of(virksomhet.organisasjonsnummer),
            virksomhet.navnPåVirksomheten
        )
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
            .bruttoInntekt(it.inntektEtterEndring.toBigDecimal())
            .erVarigEndring(true)
            .endringDato(it.dato)
            .endringBegrunnelse(it.forklaring)
    } ?: infoBuilder.erVarigEndring(false)

    yrkesaktivSisteTreFerdigliknedeÅrene?.let {
        infoBuilder.erNyIArbeidslivet(true)
    }

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
