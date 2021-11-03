package no.nav.helse.kafka

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.endringsmelding.KomplettEndringsmelding
import no.nav.helse.somJson
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject
import org.slf4j.LoggerFactory

class KafkaProducer(
    val kafkaConfig: KafkaConfig
) : HealthCheck {
    private val NAME = "EndringsmeldingProducer"
    private val ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC = TopicUse(
        name = Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN,
        valueSerializer = SøknadSerializer()
    )
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)
    private val producer = KafkaProducer(
        kafkaConfig.producer(NAME),
        ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.keySerializer(),
        ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.valueSerializer
    )

    internal fun produserKafkaMelding(
        komplettEndringsmelding: KomplettEndringsmelding,
        metadata: Metadata
    ) {
        if (metadata.version != 1) throw IllegalStateException("Kan ikke legge melding med versjon ${metadata.version} til prosessering.")
        val recordMetaData = producer.send(
            ProducerRecord(
                ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name,
                komplettEndringsmelding.k9Format.søknadId.id,
                TopicEntry(
                    metadata = metadata,
                    data = JSONObject(komplettEndringsmelding.somJson())
                )
            )
        ).get()

        logger.info(
            "Endringsmelding med søknadID: {} sendes til topic {} med offset '{}' til partition '{}'",
            komplettEndringsmelding.k9Format.søknadId.id,
            ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name,
            recordMetaData.offset(),
            recordMetaData.partition()
        )
    }

    internal fun stop() = producer.close()

    override suspend fun check(): Result {
        return try {
            producer.partitionsFor(ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name)
            Healthy(NAME, "Tilkobling til Kafka OK!")
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            UnHealthy(NAME, "Feil ved tilkobling mot Kafka. ${cause.message}")
        }
    }
}

private class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
    override fun serialize(topic: String, data: TopicEntry<JSONObject>): ByteArray {
        val metadata = JSONObject()
            .put("correlationId", data.metadata.correlationId)
            .put("version", data.metadata.version)

        return JSONObject()
            .put("metadata", metadata)
            .put("data", data.data)
            .toString()
            .toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}

