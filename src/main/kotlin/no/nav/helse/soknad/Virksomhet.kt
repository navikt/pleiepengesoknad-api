package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

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
    val regnskapsfører: Regnskapsfører? = null
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


internal fun Virksomhet.validate(index: Int): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()
    val felt = "selvstendigVirksomheter[$index]"

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

    return violations
}

internal fun Virksomhet.harGyldigPeriode(): Boolean {

    if (tilOgMed == null) return true // er pågående
    return fraOgMed <= tilOgMed
}

private fun Virksomhet.erRegistrertINorgeGyldigSatt(): Boolean {
    return !organisasjonsnummer.isNullOrBlank()
}

private fun Virksomhet.erRegistrertIUtlLandetGyldigSatt(): Boolean = registrertIUtlandet !== null
private fun Virksomhet.erVirksomhetIUtlandet(): Boolean = !registrertINorge
private fun Virksomhet.erVirksomhetINorge() = registrertINorge && registrertIUtlandet == null