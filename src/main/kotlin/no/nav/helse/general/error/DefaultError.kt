package no.nav.helse.general.error

import java.net.URI

data class DefaultError (
    val type: URI = URI.create("about:blank"),
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: URI = URI.create("about:blank")
)