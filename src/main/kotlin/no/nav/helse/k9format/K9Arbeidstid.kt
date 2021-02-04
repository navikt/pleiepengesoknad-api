package no.nav.helse.k9format

import no.nav.helse.soker.Søker
import no.nav.helse.soknad.Frilans
import no.nav.helse.soknad.Søknad
import no.nav.helse.soknad.Virksomhet
import no.nav.k9.søknad.felles.aktivitet.Arbeidstaker
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo

internal fun Søknad.byggK9Arbeidstid(søker: Søker): Arbeidstid {
    val frilanserArbeidstidInfo = frilans?.tilK9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))
    val selvstendigNæringsdrivendeArbeidstidInfo = selvstendigVirksomheter.tilK9ArbeidstidInfo()
    val arbeidstakerList: List<Arbeidstaker> =
        arbeidsgivere.tilK9Arbeidstaker(søker.fødselsnummer, Periode(fraOgMed, tilOgMed))

    return Arbeidstid(arbeidstakerList, frilanserArbeidstidInfo, selvstendigNæringsdrivendeArbeidstidInfo)
}

fun Frilans.tilK9ArbeidstidInfo(periode: Periode): ArbeidstidInfo {
    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    perioder[periode] = ArbeidstidPeriodeInfo(
        null //TODO Mangler denne verdien i brukerdialog
    )

    return ArbeidstidInfo(null, perioder) //TODO Mangler denne verdien i brukerdialog
}

fun List<Virksomhet>.tilK9ArbeidstidInfo(): ArbeidstidInfo? {
    if (isEmpty()) return null
    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    forEach { virksomhet ->
        //TODO Er dette riktig å bruke periode fra virksomheten eller periode for søknadsperioden
        perioder[Periode(virksomhet.fraOgMed, virksomhet.tilOgMed)] =
            ArbeidstidPeriodeInfo(null) //TODO Mangler denne verdien i brukerdialog
    }

    return ArbeidstidInfo(null, perioder) //TODO Mangler denne verdien i brukerdialog
}