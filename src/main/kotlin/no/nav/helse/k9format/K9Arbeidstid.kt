package no.nav.helse.k9format

import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.Søknad
import no.nav.helse.soknad.Virksomhet
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo

internal fun Søknad.byggK9Arbeidstid(): Arbeidstid {
    val arbeidstakerList: List<Arbeidstaker> = arbeidsgivere.tilK9Arbeidstaker(Periode(fraOgMed, tilOgMed))
    val frilanserArbeidstidInfo =
        frilans?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed)) //TODO 08.02.2021 - Når vi har nok info
    val selvstendigNæringsdrivendeArbeidstidInfo =
        selvstendigVirksomheter.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed), selvstendigArbeidsforhold) //TODO 08.02.2021 - Når vi har nok info

    val arbeidstid = Arbeidstid()
        .medArbeidstaker(arbeidstakerList)

    frilanserArbeidstidInfo?.let { arbeidstid.medFrilanserArbeidstid(it) }
    selvstendigNæringsdrivendeArbeidstidInfo?.let { arbeidstid.medSelvstendigNæringsdrivendeArbeidstidInfo(it) }

    return arbeidstid
}

fun Frilans.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo? {
    if (arbeidsforhold == null) return null // TODO: 20/05/2021 Må endres til at sluttdato må være innenfor søknadsperiode.

    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    perioder[periode] = ArbeidstidPeriodeInfo()
        .medJobberNormaltTimerPerDag(arbeidsforhold!!.jobberNormaltTimer.tilDuration())
        .medFaktiskArbeidTimerPerDag(arbeidsforhold.skalJobbeTimer.tilDuration())

    return ArbeidstidInfo(perioder)
}

fun List<Virksomhet>.tilK9ArbeidstidInfo(periode: Periode, selvstendigArbeidsforhold: Arbeidsforhold?): ArbeidstidInfo? {
    if (isEmpty()) return null
    if (selvstendigArbeidsforhold == null) return null

    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    forEach { virksomhet ->
        //TODO Er dette riktig å bruke periode fra virksomheten eller periode for søknadsperioden
        perioder[Periode(periode.fraOgMed, periode.tilOgMed)] =
            ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(selvstendigArbeidsforhold.jobberNormaltTimer.tilDuration())
                .medFaktiskArbeidTimerPerDag(selvstendigArbeidsforhold.jobberNormaltTimer.tilDuration())
    }

    return ArbeidstidInfo(perioder)
}
