package no.nav.helse.soker


data class Soker (
    val fodselsnummer: String,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val kjonn: String
)