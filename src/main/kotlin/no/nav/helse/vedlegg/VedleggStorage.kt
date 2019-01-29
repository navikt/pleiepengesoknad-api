package no.nav.helse.vedlegg

interface VedleggStorage {
    fun lagreVedlegg(vedlegg: Vedlegg) : VedleggId
    fun hentVedlegg(vedleggId: VedleggId) : Vedlegg?
    fun slettVedleg(vedleggId: VedleggId)
}