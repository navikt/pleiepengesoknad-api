package no.nav.helse.k9format

import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.Søknad
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo

internal fun Søknad.byggK9Arbeidstid(): Arbeidstid {
    val frilanserArbeidstidInfo = frilans?.arbeidsforhold?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))

    val selvstendigNæringsdrivendeArbeidstidInfo =
        selvstendigArbeidsforhold?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))

    val arbeidstid = Arbeidstid()
        .medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(Periode(fraOgMed, tilOgMed)))

    frilanserArbeidstidInfo?.let { arbeidstid.medFrilanserArbeidstid(it) }
    selvstendigNæringsdrivendeArbeidstidInfo?.let { arbeidstid.medSelvstendigNæringsdrivendeArbeidstidInfo(it) }

    return arbeidstid
}

fun Arbeidsforhold.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo {
    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    perioder[Periode(periode.fraOgMed, periode.tilOgMed)] =
        ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(jobberNormaltTimer.tilDuration())
            .medFaktiskArbeidTimerPerDag(skalJobbeTimer.tilDuration())

    return ArbeidstidInfo(perioder)
}
