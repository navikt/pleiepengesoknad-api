package no.nav.pleiepenger.api.barn.sparkel

import java.time.LocalDate

data class SparkelBarn (
    val fodselsdato : LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)