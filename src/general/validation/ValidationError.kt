package no.nav.pleiepenger.api.general.validation

import java.net.URI

data class ValidationError (
    val type: URI = URI.create("about:blank"),
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: URI = URI.create("about:blank"),
    val invalidParameters: List<Violation> = emptyList()
)