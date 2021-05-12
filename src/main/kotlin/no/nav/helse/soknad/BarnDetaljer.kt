package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.soknad.BarnDetaljer")

data class BarnDetaljer(
    var fødselsnummer: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fødselsdato: LocalDate?,
    val aktørId: String?,
    val navn: String?
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=***, navn=***, fodselsdato=***"
    }

    fun manglerIdentitetsnummer(): Boolean = fødselsnummer.isNullOrEmpty()

    infix fun oppdaterFødselsnummer(fødselsnummer: String?){
        logger.info("Oppdaterer fnr på barn")
        this.fødselsnummer = fødselsnummer
    }
}

internal fun BarnDetaljer.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    if(fødselsnummer.isNullOrEmpty()){
        violations.add(
            Violation(
                parameterName = "barn.fødselsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Fødselsnummer må være satt",
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