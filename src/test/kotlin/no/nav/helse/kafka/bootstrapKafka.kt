package no.nav.helse.kafka

import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment

private const val kafkaTopic = "private-pleiepengesoknad-inn"
private const val kafkaUsername = "srvkafkaclient"
private const val kafkaPassword = "kafkaclient"

fun bootstrapKafka() : KafkaEnvironment {
    return KafkaEnvironment(
        users = listOf(JAASCredential(kafkaUsername, kafkaPassword)),
        autoStart = true,
        withSchemaRegistry = false,
        withSecurity = true,
        topics = listOf(kafkaTopic)
    )
}

fun KafkaEnvironment.getUsername() : String {
    return kafkaUsername
}

fun KafkaEnvironment.getPassword() : String {
    return kafkaPassword
}