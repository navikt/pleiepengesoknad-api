package no.nav.helse.vedlegg

import io.ktor.http.ContentType

data class Vedlegg(
    val content: ByteArray,
    val contentType: ContentType
) {
    fun getSize() : Int {
        return content.size
    }
}