package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Virksomhet(
    val naringstype: List<Naringstype>,
    @JsonProperty("fisker_er_pa_blad_b")
    val fiskerErPåBladB: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
    val naringsinntekt: Int? = null,
    val navnPaVirksomheten: String,
    val organisasjonsnummer: String? = null,
    @JsonProperty("registrert_i_norge")
    val registrertINorge: Boolean,
    @JsonProperty("registrert_i_land")
    val registrertILand: String? = null,
    val yrkesaktivSisteTreFerdigliknedeArene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsforer: Regnskapsforer? = null,
    val revisor: Revisor? = null
)

data class YrkesaktivSisteTreFerdigliknedeArene(
    val oppstartsdato: LocalDate
)

enum class Naringstype(val detaljert: String) {
    @JsonProperty("FISKE") FISKER("FISKE"),
    @JsonProperty("JORDBRUK_SKOGBRUK") JORDBRUK("JORDBRUK_SKOGBRUK"),
    @JsonProperty("ANNEN") ANNET("ANNEN"),
    DAGMAMMA("DAGMAMMA")
}

data class VarigEndring(
    val dato: LocalDate,
    val inntektEtterEndring: Int,
    val forklaring: String
)

data class Revisor(
    val navn: String,
    val telefon: String,
    val erNarVennFamilie: Boolean,
    val kanInnhenteOpplysninger: Boolean?
)

data class Regnskapsforer(
    val navn: String,
    val telefon: String,
    val erNarVennFamilie: Boolean
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
