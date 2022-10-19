package no.nav.helse.soknad

import no.nav.helse.barn.BarnService
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.general.CallId
import no.nav.helse.general.Metadata
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.kafka.KafkaProducer
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.validate
import no.nav.helse.vedlegg.DokumentEier
import no.nav.helse.vedlegg.Vedlegg.Companion.validerVedlegg
import no.nav.helse.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class SøknadService(
    private val vedleggService: VedleggService,
    private val søkerService: SøkerService,
    private val barnService: BarnService,
    private val kafkaProducer: KafkaProducer
    ) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        metadata: Metadata
    ) {
        logger.info("Registrerer søknad")

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)
        søker.validate()

        logger.info("Oppdaterer barn med identitetsnummer")
        val listeOverBarnMedFnr = barnService.hentNaaverendeBarn(idToken, callId)
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)

        søknad.validate()
        val k9FormatSøknad = søknad.tilK9Format(søknad.mottatt, søker).apply {
            validerK9Format(this)
        }

        if(søknad.vedlegg.isNotEmpty()){
            val dokumentEier = DokumentEier(søker.fødselsnummer)
            logger.info("Validerer ${søknad.vedlegg.size} vedlegg")
            val vedleggHentet = vedleggService.hentVedlegg(søknad.vedlegg, idToken, callId, dokumentEier)
            vedleggHentet.validerVedlegg(søknad.vedlegg, "vedlegg")

            logger.info("Persisterer vedlegg")
            vedleggService.persisterVedlegg(søknad.vedlegg, callId, dokumentEier)
        }

        if(!søknad.opplastetIdVedleggUrls.isNullOrEmpty()){
            val dokumentEier = DokumentEier(søker.fødselsnummer)
            logger.info("Validerer ${søknad.opplastetIdVedleggUrls.size} opplastetIdVedlegg")
            val vedleggHentet = vedleggService.hentVedlegg(søknad.opplastetIdVedleggUrls, idToken, callId, dokumentEier)
            vedleggHentet.validerVedlegg(søknad.opplastetIdVedleggUrls, "opplastetIdVedleggUrls")

            logger.info("Persisterer vedlegg")
            vedleggService.persisterVedlegg(søknad.vedlegg, callId, dokumentEier)

            logger.info("Persisterer opplastetIdVedleggUrls")
            vedleggService.persisterVedlegg(søknad.opplastetIdVedleggUrls, callId, dokumentEier)
        }

        val komplettSøknad = søknad.tilKomplettSøknad(k9FormatSøknad, søker)
        try {
            kafkaProducer.produserPleiepengerMelding(komplettSøknad, metadata)
        } catch (exception: Exception) {
            logger.info("Feilet ved å legge melding på Kafka.")
            if(komplettSøknad.vedleggId.isNotEmpty()){
                logger.info("Fjerner hold på persisterte vedlegg")
                vedleggService.fjernHoldPåPersistertVedlegg(komplettSøknad.vedleggId, callId, DokumentEier(søker.fødselsnummer))
            }
            if(komplettSøknad.opplastetIdVedleggId.isNotEmpty()){
                logger.info("Fjerner hold på persisterte opplastetIdVedleggId")
                vedleggService.fjernHoldPåPersistertVedlegg(komplettSøknad.opplastetIdVedleggId, callId, DokumentEier(søker.fødselsnummer))
            }
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }
}

class MeldingRegistreringFeiletException(s: String) : Throwable(s)
