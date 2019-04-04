package no.nav.helse.vedlegg

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import no.nav.helse.general.error.DefaultError
import java.net.URI

fun StatusPages.Configuration.vedleggStatusPages() {

    val unsupportedAttachementType = URI.create("/errors/unsupported-attachment-type")
    val invalidParametersTitle = "At least one of the attachments has a not supported type. Supported are JPEG, PNG and PDF"

    exception<UnsupportedAttachementTypeException> { cause ->

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