package no.nav.helse.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.SoknadKafkaProducer")
private val TOPIC = "private-pleiepengesoknad-inn"

class SoknadKafkaProducer(private val bootstrapServers : String,
                          private val username : String,
                          private val password : String,
                          private val objectMapper: ObjectMapper) {

    private val producer = KafkaProducer<String, String>(Properties().apply {
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
        put(SaslConfigs.SASL_MECHANISM, "PLAIN")
        put(
            SaslConfigs.SASL_JAAS_CONFIG,
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        )
    })

    fun produce(soknad: KomplettSoknad) {
        val serializedSoknad = objectMapper.writeValueAsString(soknad)
        logger.trace("SerializedSoknad={}", serializedSoknad)
        val result = producer.send(ProducerRecord(TOPIC, serializedSoknad))
        logger.trace("RecordMetadata='{}'", result.get())
    }
}