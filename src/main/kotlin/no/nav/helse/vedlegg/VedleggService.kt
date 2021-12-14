package no.nav.helse.vedlegg

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import java.net.URL

class VedleggService(
    private val k9MellomlagringGateway: K9MellomlagringGateway
) {
    suspend fun lagreVedlegg(
        vedlegg: Vedlegg,
        idToken: IdToken,
        callId: CallId
    ) : VedleggId {

        return k9MellomlagringGateway.lagreVedlegg(
            vedlegg = vedlegg,
            idToken = idToken,
            callId = callId
        )

    }

    suspend fun hentVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId,
        eier: DokumentEier
    ) : Vedlegg? {

        return k9MellomlagringGateway.hentVedlegg(
            vedleggId = vedleggId,
            idToken = idToken,
            callId = callId,
            eier = eier
        )
    }

    suspend fun hentVedlegg(
        vedleggUrls: List<URL>,
        idToken: IdToken,
        callId: CallId,
        eier: DokumentEier
    ) : List<Vedlegg> {
        val vedlegg = coroutineScope {
            val futures = mutableListOf<Deferred<Vedlegg?>>()
            vedleggUrls.forEach {
                futures.add(async { hentVedlegg(
                    vedleggId = vedleggIdFromUrl(it),
                    idToken = idToken,
                    callId = callId,
                    eier = eier
                )})

            }
            futures.awaitAll().filter { it != null }
        }
        return vedlegg.requireNoNulls()
    }

    suspend fun slettVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId,
        eier: DokumentEier
    ) : Boolean{
       return k9MellomlagringGateway.slettVedlegg(
            vedleggId = vedleggId,
            idToken = idToken,
            callId = callId,
            eier = eier
        )
    }

    internal suspend fun persisterVedlegg(
        vedleggsUrls: List<URL>,
        callId: CallId,
        eier: DokumentEier
    ) {
        val vedleggsId = vedleggsUrls.map { vedleggIdFromUrl(it) }

        k9MellomlagringGateway.persisterVedlegg(
            vedleggId = vedleggsId,
            callId = callId,
            eier = eier
        )
    }

    suspend fun fjernHoldPåPersistertVedlegg(
        vedleggsUrls: List<URL>,
        callId: CallId,
        eier: DokumentEier
    ) {
        val vedleggsId = vedleggsUrls.map { vedleggIdFromUrl(it) }

        k9MellomlagringGateway.fjernHoldPåPersistertVedlegg(
            vedleggId = vedleggsId,
            callId = callId,
            eier = eier
        )
    }

}

fun vedleggIdFromUrl(url: URL) : VedleggId {
    return VedleggId(url.path.substringAfterLast("/"))
}