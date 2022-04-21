package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.helse.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.LocalDate

data class SelvstendigNæringsdrivende(
    val harInntektSomSelvstendig: Boolean,
    val virksomhet: Virksomhet? = null,
    val arbeidsforhold: Arbeidsforhold? = null
){

    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return arbeidsforhold?.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
            ?: ArbeidstidInfo()
                .medPerioder(
                    mapOf(no.nav.k9.søknad.felles.type.Periode(fraOgMed, tilOgMed) to
                            ArbeidstidPeriodeInfo()
                                .medJobberNormaltTimerPerDag(NULL_TIMER)
                                .medFaktiskArbeidTimerPerDag(NULL_TIMER))
                )
    }
}

data class Virksomhet(
    val næringstyper: List<Næringstyper> = listOf(),
    val fiskerErPåBladB: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
    val næringsinntekt: Int? = null,
    val navnPåVirksomheten: String,
    val organisasjonsnummer: String? = null,
    val registrertINorge: Boolean,
    val registrertIUtlandet: Land? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeÅrene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val harFlereAktiveVirksomheter: Boolean? = null
)

data class YrkesaktivSisteTreFerdigliknedeÅrene(
    val oppstartsdato: LocalDate
)

enum class Næringstyper {
    FISKE,
    JORDBRUK_SKOGBRUK,
    DAGMAMMA,
    ANNEN
}

data class VarigEndring(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dato: LocalDate,
    val inntektEtterEndring: Int,
    val forklaring: String
)

data class Regnskapsfører(
    val navn: String,
    val telefon: String
)


internal fun Virksomhet.validate(): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()
    val felt = "selvstendingNæringsdrivende.virksomhet"

    if(!harGyldigPeriode()){
        violations.add(
            Violation(
                parameterName = "virksomhet.tilogmed og virksomhet.fraogmed",
                parameterType = ParameterType.ENTITY,
                reason = "Har ikke gyldig periode. Fraogmed kan ikke være nyere enn tilogmed",
                invalidValue = tilOgMed
            )
        )
    }

    when {
        erVirksomhetIUtlandet() -> {
            when {
                erRegistrertIUtlLandetGyldigSatt() -> {
                    violations.addAll(registrertIUtlandet!!.valider("${felt}.registrertIUtlandet"))
                }
                else -> {
                    violations.add(
                        Violation(
                            parameterName = "${felt}.registrertIUtlandet",
                            parameterType = ParameterType.ENTITY,
                            reason = "Hvis registrertINorge er false må registrertIUtlandet være satt"
                        )
                    )
                }
            }
        }
        erVirksomhetINorge() -> {
            if (!erRegistrertINorgeGyldigSatt()) {
                violations.add(
                    Violation(
                        parameterName = "${felt}.organisasjonsnummer",
                        parameterType = ParameterType.ENTITY,
                        reason = "Hvis registrertINorge er true så må også organisasjonsnummer være satt",
                        invalidValue = organisasjonsnummer
                    )
                )
            }
        }
    }

    if(harFlereAktiveVirksomheter == null){
        violations.add(
            Violation(
                parameterName = "${felt}.harFlereAktiveVirksomheter",
                parameterType = ParameterType.ENTITY,
                reason = "harFlereAktiveVirksomheter må være satt til true eller false, ikke null",
                invalidValue = harFlereAktiveVirksomheter
            )
        )
    }

    return violations
}

internal fun Virksomhet.harGyldigPeriode(): Boolean {
    if (tilOgMed == null) return true // er pågående
    return fraOgMed <= tilOgMed
}

private fun Virksomhet.erRegistrertINorgeGyldigSatt(): Boolean = !organisasjonsnummer.isNullOrBlank()
private fun Virksomhet.erRegistrertIUtlLandetGyldigSatt(): Boolean = registrertIUtlandet !== null
private fun Virksomhet.erVirksomhetIUtlandet(): Boolean = !registrertINorge
private fun Virksomhet.erVirksomhetINorge() = registrertINorge && registrertIUtlandet == null