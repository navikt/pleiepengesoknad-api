package no.nav.helse.vedlegg

data class Vedlegg(
    val content: ByteArray,
    val contentType: String

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedlegg

        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}