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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

private val logger: Logger = LoggerFactory.getLogger("nav.vedleggApis")

@KtorExperimentalLocationsAPI
fun Route.vedleggApis(vedleggStorage: VedleggStorage) {

    @Location("/vedlegg")
    class NyttVedleg

    @Location("/vedlegg/{vedleggId}")
    data class EksisterendeVedlegg(val vedleggId: String)

    get<EksisterendeVedlegg> { eksisterendeVedlegg ->
        val vedlegg = vedleggStorage.hentVedlegg(VedleggId(eksisterendeVedlegg.vedleggId))
        if (vedlegg == null) {
            call.respond(HttpStatusCode.NotFound, "Vedlegg med ID ${eksisterendeVedlegg.vedleggId} ikke funnet")
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
            logger.warn("Content-Type er ${call.request.contentType()}")
            call.respond(HttpStatusCode.BadRequest, "Må sendes som multipart")
        } else {
            var vedlegg: Vedlegg? = null
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        vedlegg = Vedlegg(
                            content = part.streamProvider().readBytes(),
                            contentType = part.contentType!! // TODO
                        )
                    }
                }

                part.dispose()
            }

            if (vedlegg == null) {
                call.respond(HttpStatusCode.BadRequest, "Ingen vedlegg i requesten.")
            } else {
                val vedleggId = vedleggStorage.lagreVedlegg(vedlegg!!)
                call.respondVedlegg(vedleggId)
            }
        }
    }
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
