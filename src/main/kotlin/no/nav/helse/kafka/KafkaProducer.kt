package no.nav.helse.kafka

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.general.Metadata
import no.nav.helse.general.formaterStatuslogging
import no.nav.helse.soknad.KomplettSøknad
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
    private val NAME = "KafkaProducerPleiepenger"
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    private val PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC = TopicUse(
        name = Topics.MOTTATT_PLEIEPENGER_SYKT_BARN,
        valueSerializer = SøknadSerializer()
    )

    private val ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC = TopicUse(
        name = Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN,
        valueSerializer = EndringsmeldingSerializer()
    )

    private val endringsmeldingProducer = KafkaProducer(
        kafkaConfig.producer(NAME),
        ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.keySerializer(),
        ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.valueSerializer
    )
    private val pleiepengerSøknadProducer = KafkaProducer(
        kafkaConfig.producer(NAME),
        PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.keySerializer(),
        PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.valueSerializer
    )

    internal fun produserPleiepengerKafkaMelding(
        komplettSøknad: KomplettSøknad,
        metadata: Metadata
    ) {
        if (metadata.version != 1) throw IllegalStateException("Kan ikke legge melding med versjon ${metadata.version} til prosessering.")
        val recordMetaData = pleiepengerSøknadProducer.send(
            ProducerRecord(
                PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name,
                komplettSøknad.søknadId,
                TopicEntry(
                    metadata = metadata,
                    data = JSONObject(komplettSøknad.somJson())
                )
            )
        ).get()
        logger.info(
            formaterStatuslogging(komplettSøknad.søknadId,
                "sendes til topic ${PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name} " +
                        "med offset '${recordMetaData.offset()}' til partition '${recordMetaData.partition()}'"
            )
        )
    }

    internal fun produserKafkaEndringsmelding(
        komplettEndringsmelding: KomplettEndringsmelding,
        metadata: no.nav.helse.kafka.Metadata
    ) {
        if (metadata.version != 1) throw IllegalStateException("Kan ikke legge melding med versjon ${metadata.version} til prosessering.")
        val recordMetaData = endringsmeldingProducer.send(
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

    internal fun stop() = {
        endringsmeldingProducer.close()
        pleiepengerSøknadProducer.close()
    }

        override suspend fun check(): Result {
            return try {
                endringsmeldingProducer.partitionsFor(ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name)
                pleiepengerSøknadProducer.partitionsFor(PLEIEPENGER_SYKT_BARN_MOTTATT_TOPIC.name)
                Healthy(NAME, "Tilkobling til Kafka OK!")
            } catch (cause: Throwable) {
                logger.error("Feil ved tilkobling til Kafka", cause)
                UnHealthy(NAME, "Feil ved tilkobling mot Kafka. ${cause.message}")
            }
        }
    }
}

private class EndringsmeldingSerializer : Serializer<TopicEntry<JSONObject>> {
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