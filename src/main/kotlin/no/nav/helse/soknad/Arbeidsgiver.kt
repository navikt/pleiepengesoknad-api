package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.helse.general.krever
import no.nav.helse.general.somViolation
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null
) {
    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return arbeidsforhold?.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
            ?: ArbeidstidInfo()
                .medPerioder(
                    mapOf(
                        Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                            .medFaktiskArbeidTimerPerDag(Duration.ZERO)
                            .medJobberNormaltTimerPerDag(Duration.ZERO)
                    )
                )
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        if(arbeidsforhold != null) addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
        krever(organisasjonsnummer.erGyldigOrganisasjonsnummer(), "$felt.organisasjonsnummer må være gyldig")
        krever(!navn.isNullOrBlank(), "$felt.navn kan ikke være tomt eller kun whitespace")
    }
}

internal fun List<Arbeidsgiver>.validate() = mutableSetOf<Violation>().apply {
    this@validate.mapIndexed { index, arbeidsgiver ->
        addAll(arbeidsgiver.valider("arbeidsgiver[$index]").somViolation())
    }
}