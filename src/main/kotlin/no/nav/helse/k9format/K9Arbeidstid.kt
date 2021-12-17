package no.nav.helse.k9format

import no.nav.helse.soknad.ArbeidIPeriode
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.ArbeidsforholdAnsatt
import no.nav.helse.soknad.JobberIPeriodeSvar.JA
import no.nav.helse.soknad.JobberIPeriodeSvar.NEI
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.Søknad
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

internal fun Søknad.byggK9Arbeidstid(dagensDato: LocalDate): Arbeidstid = Arbeidstid().apply {
    val periode = Periode(fraOgMed, tilOgMed)

    arbeidsgivere?.let { medArbeidstaker(it.tilK9Arbeidstaker(periode, dagensDato)) }

    frilans?.let {
        medFrilanserArbeidstid(
            it.arbeidsforhold.beregnK9ArbeidstidInfo(
                periode,
                dagensDato,
                frilans.startdato,
                frilans.sluttdato
            )
        )
    }

    selvstendigNæringsdrivende?.let {
        medSelvstendigNæringsdrivendeArbeidstidInfo(it.arbeidsforhold.beregnK9ArbeidstidInfo(periode, dagensDato))
    }
}

fun List<ArbeidsforholdAnsatt>.tilK9Arbeidstaker(
    periode: Periode,
    dagensDato: LocalDate
): List<Arbeidstaker> {
    return this.map {
        Arbeidstaker()
            .medOrganisasjonsnummer(Organisasjonsnummer.of(it.organisasjonsnummer))
            .medArbeidstidInfo(it.arbeidsforhold.beregnK9ArbeidstidInfo(periode, dagensDato))
    }
}

fun Arbeidsforhold?.beregnK9ArbeidstidInfo(
    søknadsperiode: Periode,
    dagensDato: LocalDate,
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

    val arbeidstidInfo = ArbeidstidInfo()
    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()
    val gårsdagensDato = dagensDato.minusDays(1)

    historiskArbeid?.let {
        val fraOgMedHistorisk = søknadsperiode.fraOgMed
        val tilOgMedHistorisk =
            if (søknadsperiode.tilOgMed.isBefore(gårsdagensDato)) søknadsperiode.tilOgMed else gårsdagensDato

        it.beregnK9ArbeidstidInfo(
            fraOgMed = fraOgMedHistorisk,
            tilOgMed = tilOgMedHistorisk,
            arbeidstidInfo = arbeidstidInfo,
            normalTimerPerDag = normalTimerPerDag,
            startdato = startdato,
            sluttdato = sluttdato
        )
    }

    planlagtArbeid?.let {
        val fraOgMedPlanlagt = if (søknadsperiode.fraOgMed.isAfter(dagensDato)) søknadsperiode.fraOgMed else dagensDato
        val tilOgMedPlanlagt = søknadsperiode.tilOgMed

        it.beregnK9ArbeidstidInfo(
            fraOgMed = fraOgMedPlanlagt,
            tilOgMed = tilOgMedPlanlagt,
            arbeidstidInfo = arbeidstidInfo,
            normalTimerPerDag = normalTimerPerDag,
            startdato = startdato,
            sluttdato = sluttdato
        )
    }
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
            var faktiskArbeidstimer = when (dato.dayOfWeek) {
                DayOfWeek.MONDAY -> this.mandag ?: NULL_ARBEIDSTIMER
                DayOfWeek.TUESDAY -> this.tirsdag ?: NULL_ARBEIDSTIMER
                DayOfWeek.WEDNESDAY -> this.onsdag ?: NULL_ARBEIDSTIMER
                DayOfWeek.THURSDAY -> this.torsdag ?: NULL_ARBEIDSTIMER
                DayOfWeek.FRIDAY -> this.fredag ?: NULL_ARBEIDSTIMER
                else -> NULL_ARBEIDSTIMER
            }
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
