package no.nav.helse.vedlegg

import no.nav.helse.general.auth.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

private val logger: Logger = LoggerFactory.getLogger("nav.VedleggService")

class VedleggService(
    private val vedleggStorage: VedleggStorage
) {
    fun lagreVedlegg(vedlegg: Vedlegg, fnr : Fodselsnummer) : VedleggId {
        return vedleggStorage.lagreVedlegg(vedlegg)

    }
    fun hentVedlegg(vedleggId: VedleggId, fnr : Fodselsnummer) : Vedlegg? {
        return vedleggStorage.hentVedlegg(vedleggId)
    }

    fun hentOgSlettVedlegg(vedleggUrl: URL, fnr: Fodselsnummer) : Vedlegg? {
        val vedleggId = vedleggIdFromUrl(vedleggUrl)
        val vedlegg = hentVedlegg(vedleggId = vedleggId, fnr = fnr)
        if (vedlegg == null) {
            logger.error("Vedlegg med ID '$vedleggId' ble ikke funnet ved opphenting")
        }
        slettVedleg(vedleggId, fnr)
        return vedlegg
    }

    fun slettVedleg(vedleggId: VedleggId, fnr : Fodselsnummer) {
        return vedleggStorage.slettVedleg(vedleggId)
    }

    private fun vedleggIdFromUrl(url: URL) : VedleggId {
        val paths = url.path.split("/")
        return VedleggId(paths[paths.size-1])
    }
}