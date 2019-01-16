package no.nav.helse.monitorering

data class ReadinessResult(
    val isOk: Boolean,
    val message: String
)