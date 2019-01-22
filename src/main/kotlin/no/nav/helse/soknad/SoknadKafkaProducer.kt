package no.nav.helse.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import io.prometheus.client.Histogram
import no.nav.helse.general.monitoredOperation
import no.nav.helse.general.monitoredOperationtCounter
import no.nav.helse.monitorering.Readiness
import no.nav.helse.monitorering.ReadinessResult
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.config.SaslConfigs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.SoknadKafkaProducer")
private val TOPIC = "private-pleiepengesoknad-inn"

private val leggeTilBehandlingHistogram = Histogram.build(
    "histogram_legge_soknad_til_behandling",
    "Tidsbruk for å legge søknad til behandling"
).register()

private val leggeTilBehandlingCounter = monitoredOperationtCounter(
    name = "counter_legge_soknad_til_behandling",
    help = "Antall søknader lagt til behandling"
)

class SoknadKafkaProducer(private val bootstrapServers : String,
                          private val username : String,
                          private val password : String,
                          private val objectMapper: ObjectMapper) : Readiness {

    private val producer = KafkaProducer<String, String>(getProps())
    private val readinessProducer = KafkaProducer<String, String>(getProps(true))


    suspend fun produce(soknad: KomplettSoknad) {
        val serializedSoknad = objectMapper.writeValueAsString(soknad)
        logger.trace("SerializedSoknad={}", serializedSoknad)

        monitoredOperation<RecordMetadata>(
            operation = {
                try {
                    producer.send(ProducerRecord(TOPIC, serializedSoknad)).get()
                } catch (cause : Throwable) {
                    logger.error("Fikk ikke lagt søknad til behandling", cause)
                    throw cause
                }
            },
            histogram = leggeTilBehandlingHistogram,
            counter = leggeTilBehandlingCounter

        )
    }

    override suspend fun getResult(): ReadinessResult {
        return try {
            readinessProducer.partitionsFor(TOPIC)
            ReadinessResult(isOk = true, message = "Successfully connected to Kafka with bootstrap servers '$bootstrapServers'")
        } catch (cause: Throwable) {
            logger.warn("Kafka connection issues", cause)
            ReadinessResult(isOk = false, message = "Connecting to Kafka with bootstrap servers '$bootstrapServers' gave error '${cause.message}'")
        }
    }

    private fun getProps(readiness: Boolean = false) : Properties {
        return Properties().apply {
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(
                SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
            )
            if (readiness) {
                put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1500)
            }
        }
    }

}