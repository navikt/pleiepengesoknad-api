package no.nav.helse.vedlegg

data class Vedlegg(
    val content: ByteArray,
    val contentType: String,
    val size : Int = content.size
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedlegg

        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + size
        return result
    }
}