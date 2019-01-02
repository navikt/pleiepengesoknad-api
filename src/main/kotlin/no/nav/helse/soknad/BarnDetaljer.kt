package no.nav.helse.soknad

import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern

@ValidBarnDetaljer
data class BarnDetaljer(
    @get:Pattern(regexp = "\\d{11}") val fodselsnummer: String?,
    @get:NotBlank val fornavn: String,
    val mellomnavn: String?,
    @get:NotBlank val etternavn: String,
    @get:NotBlank val relasjon: String,
    @get:Past var fodselsdato: LocalDate?
) {
    fun medFodselsDato(fodselsdato: LocalDate){
        this.fodselsdato = fodselsdato
    }
}