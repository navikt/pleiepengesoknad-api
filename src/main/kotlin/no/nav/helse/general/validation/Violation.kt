package no.nav.helse.general.validation

data class Violation (
    val name: String,
    val reason: String? = null,
    val invalidValue: String? = null
)