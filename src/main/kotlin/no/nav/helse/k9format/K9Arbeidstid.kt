package no.nav.helse.k9format

import no.nav.helse.soknad.*
import no.nav.helse.soknad.JobberIPeriodeSvar.JA
import no.nav.helse.soknad.JobberIPeriodeSvar.NEI
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import kotlin.streams.toList

val NULL_ARBEIDSTIMER = Duration.ZERO

internal fun Søknad.byggK9Arbeidstid(): Arbeidstid = Arbeidstid().apply {
    val periode = Periode(fraOgMed, tilOgMed)

    if(arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(periode))

    frilans?.let {
        medFrilanserArbeidstid(
            it.arbeidsforhold.beregnK9ArbeidstidInfo(
                periode,
                frilans.startdato,
                frilans.sluttdato
            )
        )
    }

    selvstendigNæringsdrivende?.let {
        medSelvstendigNæringsdrivendeArbeidstidInfo(it.arbeidsforhold.beregnK9ArbeidstidInfo(periode))
    }
}

fun List<Arbeidsgiver>.tilK9Arbeidstaker(
    periode: Periode
): List<Arbeidstaker> {
    return this.map {
        Arbeidstaker()
            .medOrganisasjonsnummer(Organisasjonsnummer.of(it.organisasjonsnummer))
            .medArbeidstidInfo(it.arbeidsforhold.beregnK9ArbeidstidInfo(periode))
    }
}

fun Arbeidsforhold?.beregnK9ArbeidstidInfo(
    søknadsperiode: Periode,
    startdato: LocalDate? = null,
    sluttdato: LocalDate? = null
): ArbeidstidInfo {
    if (this == null) return ArbeidstidInfo().medPerioder(
        mapOf(
            Periode(søknadsperiode.fraOgMed, søknadsperiode.tilOgMed) to ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(NULL_ARBEIDSTIMER)
                .medJobberNormaltTimerPerDag(NULL_ARBEIDSTIMER)
        )
    )

    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()
    val arbeidstidInfo = ArbeidstidInfo()

    if(!harFraværIPeriode) return ArbeidstidInfo().medPerioder(
        mapOf(
            Periode(søknadsperiode.fraOgMed, søknadsperiode.tilOgMed) to ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(normalTimerPerDag)
                .medJobberNormaltTimerPerDag(normalTimerPerDag)
        )
    )

    arbeidIPeriode?.beregnK9ArbeidstidInfo(
        fraOgMed = søknadsperiode.fraOgMed,
        tilOgMed = søknadsperiode.tilOgMed,
        arbeidstidInfo = arbeidstidInfo,
        normalTimerPerDag = normalTimerPerDag,
        startdato = startdato,
        sluttdato = sluttdato
    )

    return arbeidstidInfo
}

fun ArbeidIPeriode.beregnK9ArbeidstidInfo(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    arbeidstidInfo: ArbeidstidInfo,
    startdato: LocalDate? = null,
    sluttdato: LocalDate? = null,
    normalTimerPerDag: Duration
) {
    when (jobberIPerioden) {
        JA -> {
            enkeltdager?.let {
                fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { dato ->
                    val faktiskTimerPerDag = enkeltdager.find { it.dato == dato }?.tid ?: NULL_ARBEIDSTIMER
                    arbeidstidInfo.leggTilPeriode(dato, dato, normalTimerPerDag, faktiskTimerPerDag)
                }
            }

            fasteDager?.let {
                fasteDager.tilK9ArbeidstidPeriodePlan(fraOgMed, tilOgMed, normalTimerPerDag, startdato, sluttdato)
                    .forEach {
                        arbeidstidInfo.leggeTilPeriode(it.first, it.second)
                    }
            }
        }

        //Jobber ikke. Altså 0 timer per dag i hele perioden
        NEI -> arbeidstidInfo.leggTilPeriode(fraOgMed, tilOgMed, normalTimerPerDag, NULL_ARBEIDSTIMER)
    }

}

fun ArbeidstidInfo.leggTilPeriode(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    normalTimerPerDag: Duration,
    faktiskTimerPerDag: Duration
) {
    leggeTilPeriode(
        Periode(fraOgMed, tilOgMed),
        ArbeidstidPeriodeInfo()
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)
            .medJobberNormaltTimerPerDag(normalTimerPerDag)
    )
}

fun PlanUkedager.tilK9ArbeidstidPeriodePlan(
    periodeFraOgMed: LocalDate,
    periodeTilOgMed: LocalDate,
    normalTimerPerDag: Duration,
    startdato: LocalDate? = null,
    sluttdato: LocalDate? = null,
): List<Pair<Periode, ArbeidstidPeriodeInfo>> {

    val perioder: List<Pair<Periode, ArbeidstidPeriodeInfo>> =
        periodeFraOgMed.ukedagerTilOgMed(periodeTilOgMed).map { dato ->
            var faktiskArbeidstimer = this.timerGittUkedag(dato.dayOfWeek)
            startdato?.let { if (dato.isBefore(startdato)) faktiskArbeidstimer = NULL_ARBEIDSTIMER }
            sluttdato?.let { if (dato.isAfter(sluttdato)) faktiskArbeidstimer = NULL_ARBEIDSTIMER }
            Pair(
                Periode(dato, dato),
                ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalTimerPerDag)
                    .medFaktiskArbeidTimerPerDag(faktiskArbeidstimer)
            )
        }

    return perioder
}

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> = datesUntil(tilOgMed.plusDays(1))
    .toList()
    .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }