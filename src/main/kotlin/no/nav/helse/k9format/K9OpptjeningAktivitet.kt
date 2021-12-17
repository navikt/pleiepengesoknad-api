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
        SelvstendigNæringsdrivende()
            .medVirksomhetNavn(virksomhet.navnPåVirksomheten)
            .apply { virksomhet.organisasjonsnummer?.let { medOrganisasjonsnummer(Organisasjonsnummer.of(it)) } }
            .medPerioder(
                mapOf(
                    Periode(
                        virksomhet.fraOgMed,
                        virksomhet.tilOgMed
                    ) to virksomhet.tilK9SelvstendingNæringsdrivendeInfo()
                )
            )
    )
}

internal fun Virksomhet.tilK9SelvstendingNæringsdrivendeInfo(): SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo {
    val infoBuilder = SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo()
    infoBuilder
        .medVirksomhetstyper(næringstyper.tilK9VirksomhetType())

    if (registrertINorge) {
        infoBuilder
            .medLandkode(Landkode.NORGE)
            .medRegistrertIUtlandet(false)
    } else {
        registrertIUtlandet?.let {
            infoBuilder
                .medLandkode(Landkode.of(registrertIUtlandet.landkode))
                .medRegistrertIUtlandet(true)
        }
    }

    when (erEldreEnn3År()) {
        true -> infoBuilder.medErNyoppstartet(false)
        false -> infoBuilder.medErNyoppstartet(true)
    }

    regnskapsfører?.let {
        infoBuilder
            .medRegnskapsførerNavn(regnskapsfører.navn)
            .medRegnskapsførerTlf(regnskapsfører.telefon)
    }

    næringsinntekt?.let {
        infoBuilder
            .medBruttoInntekt(næringsinntekt.toBigDecimal())
    }

    varigEndring?.let {
        infoBuilder
            .medBruttoInntekt(it.inntektEtterEndring.toBigDecimal())
            .medErVarigEndring(true)
            .medEndringDato(it.dato)
            .medEndringBegrunnelse(it.forklaring)
    } ?: infoBuilder.medErVarigEndring(false)

    yrkesaktivSisteTreFerdigliknedeÅrene?.let {
        infoBuilder.medErNyIArbeidslivet(true)
    }

    return infoBuilder
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
