package no.nav.pleiepenger.api.general.validation

data class Violation (
    val name: String,
    val reason: String? = null,
    val invalidValue: String? = null
)