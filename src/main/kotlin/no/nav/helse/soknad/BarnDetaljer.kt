package no.nav.helse.soknad

import no.nav.helse.general.auth.Fodselsnummer
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern

data class BarnDetaljer(
    @get:Pattern(regexp = "\\d{11}") var fodselsnummer: String?,
    @get:NotBlank val fornavn: String,
    @get:Pattern(regexp = "\\+*")  val mellomnavn: String?, // TODO not allow empty string
    @get:NotBlank val etternavn: String,
    @get:NotBlank val relasjon: String,
    @get:Past val fodselsdato: LocalDate
) {
    fun medFodselsnummer(fnr: Fodselsnummer){
        this.fodselsnummer = fnr.value
    }
}