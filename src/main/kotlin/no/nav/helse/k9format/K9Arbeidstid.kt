package no.nav.helse.k9format

import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.Søknad
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo

internal fun Søknad.byggK9Arbeidstid(): Arbeidstid = Arbeidstid().apply {
    arbeidsgivere.tilK9Arbeidstaker(Periode(fraOgMed, tilOgMed))
        ?.let { medArbeidstaker(it) }

    frilans?.arbeidsforhold?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))
        ?.let { medFrilanserArbeidstid(it) }

    selvstendigArbeidsforhold?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))
        ?.let { medSelvstendigNæringsdrivendeArbeidstidInfo(it) }
}

fun Arbeidsforhold.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo = ArbeidstidInfo().apply {
    medPerioder(
        mutableMapOf(
            Periode(periode.fraOgMed, periode.tilOgMed) to ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(jobberNormaltTimer.tilDuration())
                .medFaktiskArbeidTimerPerDag(skalJobbeTimer.tilDuration())
        )
    )
}
