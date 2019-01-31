package no.nav.helse.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import io.prometheus.client.Histogram
import kotlinx.serialization.toUtf8Bytes
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
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.SoknadKafkaProducer")

private val TOPIC = "privat-pleiepengesoknad-inn"

private val VERSION_HEADER_NAME = "NAV-Meldingsversjon"
private val CURRENT_VERSION = "1.0.0".toUtf8Bytes()

private val leggeTilBehandlingHistogram = Histogram.build(
    "histogram_legge_soknad_til_behandling",
    "Tidsbruk for å legge søknad til behandling"
).register()

private val leggeTilBehandlingCounter = monitoredOperationtCounter(
    name = "counter_legge_soknad_til_behandling",
    help = "Antall søknader lagt til behandling"
)

class SoknadKafkaProducer(private val bootstrapServers : String,
                          username : String,
                          password : String,
                          private val objectMapper: ObjectMapper) : Readiness {

    private val producer = KafkaProducer<String, String>(getKafkaProperties(bootstrapServers = bootstrapServers, username = username, password = password))
    private val readinessProducer = KafkaProducer<String, String>(getKafkaProperties(bootstrapServers = bootstrapServers, username = username, password = password, readiness = true))

    suspend fun produce(soknad: KomplettSoknad) {
        val serializedKafkaMessage = objectMapper.writeValueAsString(soknad)
        logger.trace("meldingsVersjon='${String(CURRENT_VERSION)}'")
        logger.trace("melding='$serializedKafkaMessage'")

        monitoredOperation<RecordMetadata>(
            operation = {
                try {
                    producer.send(
                        ProducerRecord(
                            TOPIC,
                            null,
                            null,
                            null,
                            serializedKafkaMessage,
                            listOf(RecordHeader(VERSION_HEADER_NAME, CURRENT_VERSION))
                        )
                    ).get()
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
            ReadinessResult(
                isOk = true,
                message = "Tilkobling til Kafka på bootstrap servers '$bootstrapServers' er OK"
            )
        } catch (cause: Throwable) {
            logger.warn("Kafka connection issues", cause)
            ReadinessResult(
                isOk = false,
                message = "Tilkobling til Kafka bootstrap servers '$bootstrapServers' feilet med meldingen '${cause.message}'"
            )
        }
    }
}
fun getKafkaProperties(readiness: Boolean = false,
                       bootstrapServers: String,
                       username: String,
                       password: String) : Properties {
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