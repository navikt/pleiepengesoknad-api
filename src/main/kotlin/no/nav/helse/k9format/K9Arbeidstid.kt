package no.nav.helse.k9format

import no.nav.helse.soknad.*
import no.nav.helse.soknad.JobberIPeriodeSvar.*
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

    ansatt?.let { medArbeidstaker(it.tilK9Arbeidstaker(periode, dagensDato)) }

    frilans?.arbeidsforhold?.let { medFrilanserArbeidstid(it.tilK9ArbeidstidInfo(periode, dagensDato)) }

    selvstendigNæringsdrivende?.arbeidsforhold?.let {
        medSelvstendigNæringsdrivendeArbeidstidInfo(it.tilK9ArbeidstidInfo(periode, dagensDato))
    }
}

fun List<ArbeidsforholdAnsatt>.tilK9Arbeidstaker(
    periode: Periode,
    dagensDato: LocalDate
) : List<Arbeidstaker> {
    return this.map {
        Arbeidstaker(
            null, //K9 format vil ikke ha både fnr og org nummer
            Organisasjonsnummer.of(it.organisasjonsnummer),
            it.arbeidsforhold.tilK9ArbeidstidInfo(periode, dagensDato)
        )
    }
}

fun Arbeidsforhold.tilK9ArbeidstidInfo(søknadsperiode: Periode, dagensDato: LocalDate): ArbeidstidInfo {
    val arbeidstidInfo = ArbeidstidInfo().medPerioder(null)
    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()

    historisk?.let { it.beregnHistoriskK9ArbeidstidInfo(normalTimerPerDag, søknadsperiode, arbeidstidInfo, dagensDato) }

    planlagt?.let { it.beregnPlanlagtK9ArbeidstidInfo(normalTimerPerDag, søknadsperiode, arbeidstidInfo, dagensDato) }

    return arbeidstidInfo
}

fun ArbeidIPeriode.beregnPlanlagtK9ArbeidstidInfo(
    normalTimerPerDag: Duration,
    søknadsperiode: Periode,
    arbeidstidInfo: ArbeidstidInfo,
    dagensDato: LocalDate
) {
    val fraOgMedPlanlagt = if (søknadsperiode.fraOgMed.isAfter(dagensDato)) søknadsperiode.fraOgMed else dagensDato
    val tilOgMedPlanlagt = søknadsperiode.tilOgMed

    when(jobberIPerioden) {
        JA -> when(jobberSomVanlig){
            //Jobber som vanlig. Jobber altså 100% i hele historisk periode.
            true -> arbeidstidInfo.leggTilPeriode(fraOgMedPlanlagt, tilOgMedPlanlagt, normalTimerPerDag, normalTimerPerDag)

            //Jobber redusert. Da skal enkeltdager eller fasteDager være sendt inn. Hull fylles med 0 timer.
            false -> {
                enkeltdager?.let {
                    fraOgMedPlanlagt.ukedagerTilOgMed(tilOgMedPlanlagt).forEach { dato ->
                        val faktiskTimerPerDag = enkeltdager.find { it.dato == dato }?.tid ?: NULL_ARBEIDSTIMER
                        arbeidstidInfo.leggTilPeriode(dato, dato, normalTimerPerDag, faktiskTimerPerDag)
                    }
                }

                fasteDager?.let {
                    fasteDager.tilArbeidtidPeriodePlan(søknadsperiode, dagensDato, normalTimerPerDag).forEach {
                        arbeidstidInfo.leggeTilPeriode(it.first, it.second)
                    }
                }
            }
        }

        //Jobber ikke. Altså 0 timer per dag i hele planlagt perioden
        VET_IKKE, NEI -> arbeidstidInfo.leggTilPeriode(fraOgMedPlanlagt, tilOgMedPlanlagt, normalTimerPerDag, NULL_ARBEIDSTIMER)
    }
}

fun ArbeidIPeriode.beregnHistoriskK9ArbeidstidInfo(
    normalTimerPerDag: Duration,
    søknadsperiode: Periode,
    arbeidstidInfo: ArbeidstidInfo,
    dagensDato: LocalDate
) {
    val gårsdagensdato = dagensDato.minusDays(1)
    val fraOgMedHistorisk = søknadsperiode.fraOgMed
    val tilOgMedHistorisk = if (søknadsperiode.tilOgMed.isBefore(gårsdagensdato)) søknadsperiode.tilOgMed else gårsdagensdato

    when(jobberIPerioden){
        JA -> when(jobberSomVanlig){
            //Jobber som vanlig. Altså 100% i hele historisk periode.
            true -> arbeidstidInfo.leggTilPeriode(fraOgMedHistorisk, tilOgMedHistorisk, normalTimerPerDag, normalTimerPerDag)

            //Jobber redusert -> Enkeltdager skal være satt. Hull fylles med 0 timer.
            false -> fraOgMedHistorisk.ukedagerTilOgMed(tilOgMedHistorisk).forEach { dato ->
                val faktiskTimerPerDag = enkeltdager?.find { it.dato == dato }?.tid ?: NULL_ARBEIDSTIMER
                arbeidstidInfo.leggTilPeriode(dato, dato, normalTimerPerDag, faktiskTimerPerDag)
            }
        }
        //Jobber ikke. Altså 0 timer arbeid i hele perioden.
        VET_IKKE, NEI -> arbeidstidInfo.leggTilPeriode(fraOgMedHistorisk, tilOgMedHistorisk, normalTimerPerDag, NULL_ARBEIDSTIMER)
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

fun PlanUkedager.tilArbeidtidPeriodePlan(
    periode: Periode,
    dagensDato: LocalDate = LocalDate.now(),
    normalTimerPerDag: Duration
): List<Pair<Periode, ArbeidstidPeriodeInfo>> {
    val periodeStart = if (dagensDato.isBefore(periode.fraOgMed)) periode.fraOgMed else dagensDato

    val perioder: List<Pair<Periode, ArbeidstidPeriodeInfo>> =
        periodeStart.ukedagerTilOgMed(periode.tilOgMed).mapNotNull { dato ->
            val faktiskArbeidstimer = when (dato.dayOfWeek) {
                DayOfWeek.MONDAY -> this.mandag ?: NULL_ARBEIDSTIMER
                DayOfWeek.TUESDAY -> this.tirsdag ?: NULL_ARBEIDSTIMER
                DayOfWeek.WEDNESDAY -> this.onsdag ?: NULL_ARBEIDSTIMER
                DayOfWeek.THURSDAY -> this.torsdag ?: NULL_ARBEIDSTIMER
                DayOfWeek.FRIDAY -> this.fredag ?: NULL_ARBEIDSTIMER
                else -> null
            }
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