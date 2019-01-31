package no.nav.helse.kafka

import no.nav.common.KafkaEnvironment
import no.nav.helse.soknad.getKafkaProperties
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

fun testAtDetLiggerMeldingPaaKoen(
    antallMeldinger: Int,
    kafkaEnvironment: KafkaEnvironment
  ) {
    val properties = getKafkaProperties(
        username = kafkaEnvironment.getConsumerUsername(),
        password = kafkaEnvironment.getConsumerPassword(),
        bootstrapServers = kafkaEnvironment.brokersURL
    )
    properties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
    properties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"

    val consumer = KafkaConsumer<String, String>(properties)
    consumer.subscribe(listOf(kafkaEnvironment.getTopic()))

    val records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS))

    assertEquals(antallMeldinger, records.count())
}