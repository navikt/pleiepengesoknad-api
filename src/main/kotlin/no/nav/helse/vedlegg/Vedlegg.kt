package no.nav.helse.vedlegg

import no.nav.helse.dusseldorf.ktor.core.*
import java.net.URL

data class Vedlegg(
    val content: ByteArray,
    val contentType: String,
    val title: String

) {
    companion object {

        private const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024 // 3 vedlegg på 8 MB

        private val vedleggTooLargeProblemDetails = DefaultProblemDetails(
            title = "attachments-too-large",
            status = 413,
            detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
        )

        fun List<Vedlegg>.validerVedlegg(vedleggUrler: List<URL>) {
            if (size != vedleggUrler.size) {
                throw Throwblem(
                    ValidationProblemDetails(
                        violations = setOf(
                            Violation(
                                parameterName = "vedlegg",
                                parameterType = ParameterType.ENTITY,
                                reason = "Mottok referanse til ${vedleggUrler.size} vedlegg, men fant kun $size vedlegg.",
                                invalidValue = vedleggUrler
                            )
                        )
                    )
                )
            }
            validerTotalStorresle()
        }

        private fun List<Vedlegg>.validerTotalStorresle() {
            val totalSize = sumBy { it.content.size }
            if (totalSize > MAX_VEDLEGG_SIZE) {
                throw Throwblem(vedleggTooLargeProblemDetails)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedlegg

        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }

}
