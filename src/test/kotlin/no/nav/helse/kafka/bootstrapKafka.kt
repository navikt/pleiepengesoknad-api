package no.nav.helse.kafka

import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment

private const val kafkaTopic = "privat-pleiepengesoknad-inn"
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

fun KafkaEnvironment.getProducerUsername() : String {
    return kafkaUsername
}

fun KafkaEnvironment.getProducerPassword() : String {
    return kafkaPassword
}

fun KafkaEnvironment.getConsumerUsername() : String {
    return kafkaUsername
}

fun KafkaEnvironment.getConsumerPassword() : String {
    return kafkaPassword
}

fun KafkaEnvironment.getTopic() : String {
    return kafkaTopic
}

/*
//    kafkaEnvironment.adminClient!!.createAcls(listOf(
//        AclBinding(
//            ResourcePattern(ResourceType.TOPIC, kafkaTopic, PatternType.LITERAL),
//            AccessControlEntry(kafkaProducerUsername,"127.0.0.1", AclOperation.ALL, AclPermissionType.ALLOW)
//        ),
//        AclBinding(
//            ResourcePattern(ResourceType.TOPIC, kafkaTopic, PatternType.LITERAL),
//            AccessControlEntry(kafkaConsumerUsername,"127.0.0.1", AclOperation.READ, AclPermissionType.ALLOW)
//        )
//    ))
 */

/*
internal val kafkaC1 = JAASCredential("srvkafkac1", "kafkac1")
internal val kafkaP1 = JAASCredential("srvkafkap1", "kafkap1")
internal val kafkaC2 = JAASCredential("srvkafkac2", "kafkac2")
internal val kafkaP2 = JAASCredential("srvkafkap2", "kafkap2")
 */