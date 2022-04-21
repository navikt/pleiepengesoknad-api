package no.nav.helse.k9format

import no.nav.helse.soknad.Arbeidsgiver
import no.nav.helse.soknad.Søknad
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.streams.toList

internal fun Søknad.byggK9Arbeidstid(): Arbeidstid = Arbeidstid().apply {

    if(arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(fraOgMed, tilOgMed))

    medFrilanserArbeidstid(frilans.k9ArbeidstidInfo(fraOgMed, tilOgMed))
    medSelvstendigNæringsdrivendeArbeidstidInfo(selvstendigNæringsdrivende.k9ArbeidstidInfo(fraOgMed, tilOgMed))
}

fun List<Arbeidsgiver>.tilK9Arbeidstaker(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate
): List<Arbeidstaker> {
    return this.map {
            Arbeidstaker()
                .medOrganisasjonsnummer(Organisasjonsnummer.of(it.organisasjonsnummer))
                .medArbeidstidInfo(it.k9ArbeidstidInfo(fraOgMed, tilOgMed))
    }
}

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> = datesUntil(tilOgMed.plusDays(1))
    .toList()
    .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }