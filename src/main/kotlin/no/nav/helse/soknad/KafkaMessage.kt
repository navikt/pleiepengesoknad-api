package no.nav.helse.soknad

data class KafkaMessage<T> (
    val metaData: KafkaMetadata,
    val message: T
)