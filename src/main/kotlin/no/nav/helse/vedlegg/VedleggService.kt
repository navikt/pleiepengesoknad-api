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

    fun hentVedlegg(
        vedleggUrls: List<URL>,
        fnr: Fodselsnummer
    ) : List<Vedlegg> {
        val vedleggList = mutableListOf<Vedlegg>()
        vedleggUrls.forEach {
            val vedleggId = vedleggIdFromUrl(it)
            val vedlegg = hentVedlegg(vedleggId = vedleggId, fnr = fnr)
            if (vedlegg != null) {
                vedleggList.add(vedlegg)
            }
        }
        return vedleggList
    }

    fun slettVedleg(
        vedleggId : VedleggId,
        fnr: Fodselsnummer
    ) {
        try { vedleggStorage.slettVedleg(vedleggId)} catch (cause: Throwable) {
            logger.trace("Fikk ikke slettet vedlegg $vedleggId", cause)
        }
    }

    fun slettVedleg(
        vedleggUrls: List<URL>,
        fnr: Fodselsnummer
    ) {
        vedleggUrls.forEach {
            slettVedleg(
                vedleggId = vedleggIdFromUrl(it),
                fnr = fnr
            )
        }
    }

    private fun vedleggIdFromUrl(url: URL) : VedleggId {
        val paths = url.path.split("/")
        return VedleggId(paths[paths.size-1])
    }
}