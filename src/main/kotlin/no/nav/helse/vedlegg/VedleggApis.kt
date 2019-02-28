package no.nav.helse.vedlegg

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.error.DefaultError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("nav.vedleggApis")
private const val MAX_VEDLEGG_SIZE = 8 * 1024 * 1024

private val hasToBeMultipartType = URI.create("/errors/multipart-form-required")
private const val hasToBeMultipartTitle = "Requesten må være en 'multipart/form-data' request hvor en 'part' er en fil, har 'name=vedlegg' og har Content-Type header satt."

private val vedleggNotFoundType = URI.create("/errors/attachment-not-found")
private const val vedleggNotFoundTitle = "Inget vedlegg funnet med etterspurt ID."

private val vedleggNotAttachedType = URI.create("/errors/attachment-not-attached")
private const val vedleggNotAttachedTitle = "Fant ingen 'part' som er en fil, har 'name=vedlegg' og har Content-Type header satt."

private val vedleggTooLargeType = URI.create("/errors/attachment-too-large")
private const val vedleggTooLargeTitle = "Vedlegget var over maks tillatt størrelse på 8MB."

@KtorExperimentalLocationsAPI
fun Route.vedleggApis(vedleggService: VedleggService) {

    @Location("/vedlegg")
    class NyttVedleg

    @Location("/vedlegg/{vedleggId}")
    data class EksisterendeVedlegg(val vedleggId: String)

    get<EksisterendeVedlegg> { eksisterendeVedlegg ->
        val vedleggId = VedleggId(eksisterendeVedlegg.vedleggId)
        val vedlegg = vedleggService.hentVedlegg(vedleggId, call.getFodselsnummer())

        if (vedlegg == null) {
            call.respondVedleggNotFound(vedleggId)
        } else {
            call.respondBytes(
                bytes = vedlegg.content,
                contentType = ContentType.parse(vedlegg.contentType),
                status = HttpStatusCode.OK
            )
        }
    }

    delete<EksisterendeVedlegg> { eksisterendeVedlegg ->
        vedleggService.slettVedleg(VedleggId(eksisterendeVedlegg.vedleggId), call.getFodselsnummer())
        call.respond(HttpStatusCode.NoContent)
    }

    post<NyttVedleg> { _ ->
        if (!call.request.isFormMultipart()) {
            call.respondHasToBeMultiPart()
        } else {
            val multipart = call.receiveMultipart()
            val vedlegg = multipart.getVedlegg()

            if (vedlegg == null) {
                call.respondVedleggNotAttached()
            } else {
                if (vedlegg.content.size > MAX_VEDLEGG_SIZE) {
                    call.respondVedleggTooLarge()
                } else {
                    val vedleggId = vedleggService.lagreVedlegg(vedlegg, call.getFodselsnummer())
                    call.respondVedlegg(vedleggId)
                }
            }
        }
    }
}

private suspend fun MultiPartData.getVedlegg() : Vedlegg? {
    for (partData in readAllParts()) {
        if (partData is PartData.FileItem && "vedlegg".equals(partData.name, ignoreCase = true) && partData.contentType != null) {
            val vedlegg = Vedlegg(
                content = partData.streamProvider().readBytes(),
                contentType = partData.contentType.toString(),
                title = partData.originalFileName?: "Ingen tittel tilgjengelig"
            )
            partData.dispose()
            return vedlegg
        }
        partData.dispose()
    }
    return null
}

private suspend fun ApplicationCall.respondVedleggTooLarge() {
    respond(
        HttpStatusCode.PayloadTooLarge, DefaultError(
            status = HttpStatusCode.PayloadTooLarge.value,
            type = vedleggTooLargeType,
            title = vedleggTooLargeTitle
        )
    )
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
    val url = URLBuilder(getBaseUrlFromRequest()).path("vedlegg",vedleggId.value).build().toString()
    response.header(HttpHeaders.Location, url)
    response.header(HttpHeaders.AccessControlExposeHeaders, HttpHeaders.Location)
    respond(HttpStatusCode.Created)
}

private fun ApplicationCall.getBaseUrlFromRequest() : String {
    val host = request.origin.host
    val isLocalhost = "localhost".equals(host, ignoreCase = true)
    val scheme = if (isLocalhost) "http" else "https"
    val port = if (isLocalhost) ":${request.origin.port}" else ""
    return "$scheme://$host$port"
}
