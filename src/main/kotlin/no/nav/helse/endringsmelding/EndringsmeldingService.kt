package no.nav.helse.endringsmelding

import no.nav.helse.general.Metadata
import no.nav.helse.kafka.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class EndringsmeldingService(
    private val kafkaProducer: KafkaProducer
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EndringsmeldingService::class.java)
    }

    fun registrer(
        komplettEndringsmelding: KomplettEndringsmelding,
        metadata: Metadata
    ) {
        logger.info("Registrerer endringsmelding...")

        try {
            kafkaProducer.produserEndringsmelding(komplettEndringsmelding, metadata)
        } catch (exception: Exception) {
            logger.info("Feilet ved å legge melding på Kafka.")
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }

        logger.info("Endringsmelding registerert.")
    }
}
class MeldingRegistreringFeiletException(s: String) : Throwable(s)
