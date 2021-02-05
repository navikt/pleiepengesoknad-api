package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class BarnDetaljer(
    var fødselsnummer: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fødselsdato: LocalDate?,
    val aktørId: String?,
    val navn: String?
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=${aktørId}, navn=${navn}, fodselsdato=${fødselsdato}"
    }

    fun manglerIdentitetsnummer(): Boolean = fødselsnummer.isNullOrEmpty()

    infix fun oppdaterFødselsnummer(fødselsnummer: String?){
        this.fødselsnummer = fødselsnummer
    }
}

internal fun BarnDetaljer.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    if(fødselsnummer.isNullOrEmpty() && fødselsdato == null){
        violations.add(
            Violation(
                parameterName = "barn.fødselsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Kan ikke ha null for både fødselsnummer og fødselsdato",
                invalidValue = fødselsnummer
            )
        )
    }

    if (fødselsnummer != null && !fødselsnummer!!.erGyldigNorskIdentifikator()) {
        violations.add(
            Violation(
                parameterName = "barn.fødselsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Ikke gyldig fødselsnummer.",
                invalidValue = fødselsnummer
            )
        )
    }

    if (fødselsdato != null && (fødselsdato.isAfter(LocalDate.now()))) {
        violations.add(
            Violation(
                parameterName = "barn.fødselsdato",
                parameterType = ParameterType.ENTITY,
                reason = "Fødselsdato kan ikke være i fremtiden",
                invalidValue = fødselsdato
            )
        )
    }

    return violations
}