package no.nav.helse.vedlegg

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.prometheus.client.Counter
import no.nav.helse.general.error.DefaultError
import no.nav.helse.general.error.monitorException
import java.net.URI

fun StatusPages.Configuration.vedleggStatusPages(
    errorCounter : Counter
) {

    val unsupportedAttachementType = URI.create("/errors/unsupported-attachment-type")
    val invalidParametersTitle = "At least one of the attachments has a not supported type. Supported are JPEG, PNG and PDF"

    exception<UnsupportedAttachementTypeException> { cause ->
        monitorException(cause, unsupportedAttachementType, errorCounter)

        call.respond(
            HttpStatusCode.UnprocessableEntity, DefaultError(
                status = HttpStatusCode.UnprocessableEntity.value,
                type = unsupportedAttachementType,
                title = invalidParametersTitle,
                detail = cause.message
            )
        )
        throw cause
    }
}