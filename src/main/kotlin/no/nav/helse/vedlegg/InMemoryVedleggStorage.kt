package no.nav.helse.vedlegg

import java.util.*

class InMemoryVedleggStorage : VedleggStorage {

    private val storage : MutableMap<VedleggId, Vedlegg> = mutableMapOf()

    override fun lagreVedlegg(vedlegg: Vedlegg): VedleggId {
        val vedleggId = generateVedleggId()
        storage[vedleggId] = vedlegg
        return vedleggId
    }

    override fun hentVedlegg(vedleggId: VedleggId): Vedlegg? {
        return storage[vedleggId]
    }

    override fun slettVedleg(vedleggId: VedleggId) {
        storage.remove(vedleggId)
    }

    private fun generateVedleggId() : VedleggId {
        return VedleggId(UUID.randomUUID().toString())
    }
}