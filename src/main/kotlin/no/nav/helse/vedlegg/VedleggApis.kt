package no.nav.helse.vedlegg

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import no.nav.helse.general.error.DefaultError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("nav.vedleggApis")

private val hasToBeMultipartType = URI.create("/errors/multipart-form-required")
private const val hasToBeMultipartTitle = "Requesten må være en 'multipart/form-data' request hvor en 'part' er en fil, har 'name=vedlegg' og har Content-Type header satt."

private val vedleggNotFoundType = URI.create("/errors/attachment-not-found")
private const val vedleggNotFoundTitle = "Inget vedlegg funnet med etterspurt ID."

private val vedleggNotAttachedType = URI.create("/errors/attachment-not-attached")
private const val vedleggNotAttachedTitle = "Fant ingen 'part' som er en fil, har 'name=vedlegg' og har Content-Type header satt."

@KtorExperimentalLocationsAPI
fun Route.vedleggApis(vedleggStorage: VedleggStorage) {

    @Location("/vedlegg")
    class NyttVedleg

    @Location("/vedlegg/{vedleggId}")
    data class EksisterendeVedlegg(val vedleggId: String)

    get<EksisterendeVedlegg> { eksisterendeVedlegg ->
        val vedleggId = VedleggId(eksisterendeVedlegg.vedleggId)
        val vedlegg = vedleggStorage.hentVedlegg(vedleggId)

        if (vedlegg == null) {
            call.respondVedleggNotFound(vedleggId)
        } else {
            call.respondBytes(
                bytes = vedlegg.content,
                contentType = vedlegg.contentType,
                status = HttpStatusCode.OK
            )
        }
    }

    delete<EksisterendeVedlegg> { eksisterendeVedlegg ->
        vedleggStorage.slettVedleg(VedleggId(eksisterendeVedlegg.vedleggId))
        call.respond(HttpStatusCode.NoContent)
    }

    post<NyttVedleg> { nyttVedlegg ->
        if (!call.request.isFormMultipart()) {
            call.respondHasToBeMultiPart()
        } else {
            var vedlegg: Vedlegg? = null
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.contentType != null && "vedlegg".equals(part.name)) {
                            vedlegg = Vedlegg(
                                content = part.streamProvider().readBytes(),
                                contentType = part.contentType!!
                            )
                        }
                    }
                }
                part.dispose()
            }

            if (vedlegg == null) {
                call.respondVedleggNotAttached()
            } else {
                val vedleggId = vedleggStorage.lagreVedlegg(vedlegg!!)
                call.respondVedlegg(vedleggId)
            }
        }
    }
}

private suspend fun ApplicationCall.respondVedleggNotAttached() {
    respond(
        HttpStatusCode.BadRequest, DefaultError(
            status = HttpStatusCode.BadRequest.value,
            type = vedleggNotAttachedType,
            title = vedleggNotAttachedTitle
        )
    )
}

private suspend fun ApplicationCall.respondHasToBeMultiPart() {
    respond(
        HttpStatusCode.BadRequest, DefaultError(
            status = HttpStatusCode.BadRequest.value,
            type = hasToBeMultipartType,
            title = hasToBeMultipartTitle
        )
    )
}

private suspend fun ApplicationCall.respondVedleggNotFound(vedleggId : VedleggId) {
    respond(
        HttpStatusCode.NotFound, DefaultError(
            status = HttpStatusCode.NotFound.value,
            type = vedleggNotFoundType,
            title = vedleggNotFoundTitle,
            detail = "Vedlegg med ID ${vedleggId.value} ikke funnet."
        )
    )
}

private fun ApplicationRequest.isFormMultipart(): Boolean {
    return contentType().withoutParameters().match(ContentType.MultiPart.FormData)
}

private suspend fun ApplicationCall.respondVedlegg(vedleggId: VedleggId) {
    val url = URLBuilder(getBaseUrlFromRequest()).path("vedlegg",vedleggId.value).buildString()
    response.header(HttpHeaders.Location, url)
    respond(HttpStatusCode.Created)
}

private fun ApplicationCall.getBaseUrlFromRequest() : String {
    return "${request.origin.scheme}://${request.origin.remoteHost}${request.origin.exposedPortPart()}"
}

private fun RequestConnectionPoint.exposedPortPart(): String {
    return if ("http".equals(scheme, ignoreCase = true) && port != 80 || "https".equals(scheme, ignoreCase = true) && port != 443) ":$port" else ""
}
