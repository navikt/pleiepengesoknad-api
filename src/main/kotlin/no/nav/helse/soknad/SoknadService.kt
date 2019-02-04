package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.soker.SokerService
import no.nav.helse.vedlegg.Vedlegg
import no.nav.helse.vedlegg.VedleggService
import java.net.URL

class SoknadService(val soknadKafkaProducer: SoknadKafkaProducer,
                    val sokerService: SokerService,
                    val vedleggService: VedleggService) {

    suspend fun registrer(
        soknad: Soknad,
        fnr: Fodselsnummer,
        callId: CallId
    ) {

        val komplettSoknad = KomplettSoknad(
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = sokerService.getSoker(fnr = fnr, callId = callId),
            barn = soknad.barn,
            vedlegg = hentVedlegg(vedleggUrls = soknad.vedlegg, fnr = fnr),
            ansettelsesforhold = soknad.ansettelsesforhold
        )

        soknadKafkaProducer.produce(komplettSoknad)
    }

    private fun hentVedlegg(vedleggUrls: List<URL>, fnr: Fodselsnummer) : List<Vedlegg> {
        val vedleggListe = mutableListOf<Vedlegg>()
        vedleggUrls.forEach { url ->
            val vedlegg = vedleggService.hentOgSlettVedlegg(vedleggUrl = url, fnr = fnr)
            if (vedlegg != null) {
                vedleggListe.add(vedlegg)
            }
        }
        return vedleggListe.toList()
    }
}