package no.nav.helse.barn.sparkel

data class SparkelBarn (
    val fodselsnummer: String,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)