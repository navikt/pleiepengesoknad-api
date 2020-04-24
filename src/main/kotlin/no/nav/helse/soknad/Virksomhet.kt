package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Virksomhet(
    val næringstyper: List<Næringstyper> = listOf(),
    @JsonProperty("fisker_er_pa_blad_b")
    val fiskerErPåBladB: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate? = null,
    val næringsinntekt: Int? = null,
    val navnPåVirksomheten: String,
    val organisasjonsnummer: String? = null,
    @JsonProperty("registrert_i_norge")
    val registrertINorge: Boolean,
    @JsonProperty("registrert_i_land")
    val registrertILand: String? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeÅrene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val revisor: Revisor? = null
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

data class Revisor(
    val navn: String,
    val telefon: String,
    val kanInnhenteOpplysninger: Boolean
)

data class Regnskapsfører(
    val navn: String,
    val telefon: String
)


internal fun Virksomhet.validate(): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

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

    if(!erRegistrertINorgeGyldigSatt()){
        violations.add(
            Violation(
                parameterName = "organisasjonsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis registrertINorge er true så må også organisasjonsnummer være satt",
                invalidValue = organisasjonsnummer
            )
        )
    }

    if(!erRegistrertILandGyldigSatt()){
        violations.add(
            Violation(
                parameterName = "registrertILand",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis registrertINorge er false så må registrertILand være satt til noe",
                invalidValue = registrertILand
            )
        )
    }

    return violations
}

internal fun Virksomhet.harGyldigPeriode(): Boolean {

    if (tilOgMed == null) return true // er pågående
    return fraOgMed <= tilOgMed
}

internal fun Virksomhet.erRegistrertINorgeGyldigSatt(): Boolean{
    if(registrertINorge) return !organisasjonsnummer.isNullOrBlank()
    return true
}

internal fun Virksomhet.erRegistrertILandGyldigSatt(): Boolean{
    if(!registrertINorge) return registrertILand != null && !registrertILand.isNullOrBlank()
    return true
}
