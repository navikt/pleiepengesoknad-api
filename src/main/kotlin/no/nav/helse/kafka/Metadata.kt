package no.nav.helse.kafka

data class Metadata(
    val version : Int,
    val correlationId : String
)
