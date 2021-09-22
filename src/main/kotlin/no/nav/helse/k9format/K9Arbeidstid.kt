package no.nav.helse.k9format

import no.nav.helse.soknad.*
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
    arbeidsgivere.tilK9Arbeidstaker(Periode(fraOgMed, tilOgMed), dagensDato)
        ?.let { medArbeidstaker(it) }

    frilans?.arbeidsforhold?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))
        ?.let { medFrilanserArbeidstid(it) }

    selvstendigArbeidsforhold?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))
        ?.let { medSelvstendigNæringsdrivendeArbeidstidInfo(it) }
}

internal fun ArbeidsgiverDetaljer.tilK9Arbeidstaker(
    periode: Periode,
    dagensDato: LocalDate
): List<Arbeidstaker>? {
    if (organisasjoner.isEmpty()) return null

    return organisasjoner.map { organisasjon ->
        Arbeidstaker(
            null, //K9 format vil ikke ha både fnr og org nummer
            Organisasjonsnummer.of(organisasjon.organisasjonsnummer),
            if(organisasjon.historisk != null || organisasjon.planlagt != null) organisasjon.tilK9ArbeidstidInfoV2(periode, dagensDato) else organisasjon.tilK9ArbeidstidInfo(periode)
        )
    }
}

fun Arbeidsforhold.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo = ArbeidstidInfo().apply {
    val faktiskTimerPerUke = jobberNormaltTimer.tilFaktiskTimerPerUke(skalJobbeProsent)
    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()
    val faktiskArbeidstimerPerDag = faktiskTimerPerUke.tilTimerPerDag().tilDuration()

    medPerioder(
        mutableMapOf(
            periode to ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normalTimerPerDag)
                .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)
        )
    )
}

fun OrganisasjonDetaljer.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo = ArbeidstidInfo().apply {
    val faktiskTimerPerUke = jobberNormaltTimer.tilFaktiskTimerPerUke(skalJobbeProsent)
    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()
    val faktiskArbeidstimerPerDag = faktiskTimerPerUke.tilTimerPerDag().tilDuration()

    medPerioder(
        mutableMapOf(
            periode to ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normalTimerPerDag)
                .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)
        )
    )
}

fun OrganisasjonDetaljer.tilK9ArbeidstidInfoV2(søknadsperiode: Periode, dagensDato: LocalDate): ArbeidstidInfo {
    val arbeidstidInfo = ArbeidstidInfo().medPerioder(null)
    val normalTimerPerDag = jobberNormaltTimer.tilTimerPerDag().tilDuration()

    if(this.historisk != null) this.historisk.beregnHistoriskK9ArbeidstidInfo(normalTimerPerDag, søknadsperiode, arbeidstidInfo)

    if(this.planlagt != null) this.planlagt.beregnPlanlagtK9ArbeidstidInfo(normalTimerPerDag,søknadsperiode,arbeidstidInfo, dagensDato)

    /*
    this.planlagt?.also { arbeidIPeriodePlanlagt ->
        val fraOgMedPlanlagt = if(søknadsperiode.fraOgMed.isAfter(dagensDato)) søknadsperiode.fraOgMed else dagensDato
        val tilOgMedPlanlagt = søknadsperiode.tilOgMed

        arbeidIPeriodePlanlagt.enkeltdager?.also { listeOverEnkeltdager ->
            fraOgMedPlanlagt.ukedagerTilOgMed(tilOgMedPlanlagt).forEach { dato ->
                val enkeltdag = listeOverEnkeltdager.find { it.dato == dato } ?: Enkeltdag(dato, NULL_ARBEIDSTIMER)
                arbeidstidInfo.leggeTilPeriode(
                    Periode(enkeltdag.dato, enkeltdag.dato),
                    ArbeidstidPeriodeInfo()
                        .medJobberNormaltTimerPerDag(normalTimerPerDag)
                        .medFaktiskArbeidTimerPerDag(enkeltdag.tid)
                )
            }
        }

        arbeidIPeriodePlanlagt.fasteDager?.let { planUkedager ->
            planUkedager.tilArbeidtidPeriodePlan(periode = søknadsperiode, normalTimerPerDag = normalTimerPerDag).forEach {
                arbeidstidInfo.leggeTilPeriode(it.first, it.second)
            }
        }
    }
     */

    return arbeidstidInfo
}

fun ArbeidIPeriode.beregnPlanlagtK9ArbeidstidInfo(normalTimerPerDag: Duration, søknadsperiode: Periode, arbeidstidInfo: ArbeidstidInfo, dagensDato: LocalDate){
    val fraOgMedPlanlagt = if(søknadsperiode.fraOgMed.isAfter(dagensDato)) søknadsperiode.fraOgMed else dagensDato
    val tilOgMedPlanlagt = søknadsperiode.tilOgMed

    when(jobber){
        true -> when(jobberRedustert){
            true -> {
                if(enkeltdager != null) {
                    fraOgMedPlanlagt.ukedagerTilOgMed(tilOgMedPlanlagt).forEach { dato ->
                        val enkeltdag = enkeltdager.find { it.dato == dato } ?: Enkeltdag(dato, NULL_ARBEIDSTIMER)
                        arbeidstidInfo.leggeTilPeriode(
                            Periode(enkeltdag.dato, enkeltdag.dato),
                            ArbeidstidPeriodeInfo()
                                .medFaktiskArbeidTimerPerDag(enkeltdag.tid)
                                .medJobberNormaltTimerPerDag(normalTimerPerDag)
                        )
                    }
                } else if (fasteDager != null) {
                    fasteDager.tilArbeidtidPeriodePlan(søknadsperiode, dagensDato, normalTimerPerDag).forEach {
                        arbeidstidInfo.leggeTilPeriode(it.first, it.second)
                    }
                } else TODO() // TODO: 22/09/2021 BURDE ALDRI KOMME HIT. KASTE FEIL?
            }
            false -> TODO() //Jobber men ikke redusert. Jobber altså 100% i hele historisk periode.
        }
        false -> { //Jobber ikke. Altså 0 timer per dag i hele planlagt periode.
            arbeidstidInfo.leggeTilPeriode(
                Periode(fraOgMedPlanlagt, tilOgMedPlanlagt), ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalTimerPerDag)
                    .medFaktiskArbeidTimerPerDag(NULL_ARBEIDSTIMER)
            )
        }
    }
}

fun ArbeidIPeriode.beregnHistoriskK9ArbeidstidInfo(normalTimerPerDag: Duration, søknadsperiode: Periode, arbeidstidInfo: ArbeidstidInfo) {
    val gårsdagensdato = LocalDate.now().minusDays(1)
    val fraOgMedHistorisk = søknadsperiode.fraOgMed
    val tilOgMedHistorisk = if(søknadsperiode.tilOgMed.isBefore(gårsdagensdato)) søknadsperiode.tilOgMed else gårsdagensdato

    when (jobber) {
        true -> when (jobberRedustert) {
            true -> { //Jobber redusert -> Enkeltdager skal være satt.
                enkeltdager?.also { listeOverEnkeldager ->
                    fraOgMedHistorisk.ukedagerTilOgMed(tilOgMedHistorisk).forEach { dato ->
                        val enkeltdag = listeOverEnkeldager.find { it.dato == dato } ?: Enkeltdag(dato, NULL_ARBEIDSTIMER)
                        arbeidstidInfo.leggeTilPeriode(
                            Periode(enkeltdag.dato, enkeltdag.dato),
                            ArbeidstidPeriodeInfo()
                                .medFaktiskArbeidTimerPerDag(enkeltdag.tid)
                                .medJobberNormaltTimerPerDag(normalTimerPerDag)
                        )
                    }
                }
            }
            false -> { //Jobber men ikke redusert. Jobber altså 100% i hele historisk periode.
                arbeidstidInfo.leggeTilPeriode(
                    Periode(fraOgMedHistorisk, tilOgMedHistorisk), ArbeidstidPeriodeInfo()
                        .medJobberNormaltTimerPerDag(normalTimerPerDag)
                        .medFaktiskArbeidTimerPerDag(normalTimerPerDag)
                )
            }
        }
        false -> { //Jobber ikke. Altså 0 timer per dag i hele historisk periode.
            arbeidstidInfo.leggeTilPeriode(
                Periode(fraOgMedHistorisk, tilOgMedHistorisk), ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalTimerPerDag)
                    .medFaktiskArbeidTimerPerDag(NULL_ARBEIDSTIMER)
            )
        }
    }
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
                DayOfWeek.MONDAY -> this.mandag
                DayOfWeek.TUESDAY -> this.tirsdag
                DayOfWeek.WEDNESDAY -> this.onsdag
                DayOfWeek.THURSDAY -> this.torsdag
                DayOfWeek.FRIDAY -> this.fredag
                else -> Duration.ZERO
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

fun LocalDate.ukedagerTilOgMed(tilOgMed: LocalDate): List<LocalDate> {
    return this.datesUntil(tilOgMed.plusDays(1))
        .toList()
        .filterNot { it.dayOfWeek == DayOfWeek.SUNDAY || it.dayOfWeek == DayOfWeek.SATURDAY }
}
