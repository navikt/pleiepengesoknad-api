package no.nav.helse.mellomlagring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.ENDRINGSMELDING_MELLOMLAGRING_URL
import no.nav.helse.MELLOMLAGRING_URL
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.respondProblemDetails
import no.nav.helse.soknad.hentIdTokenOgCallId
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.net.URI

private suspend fun ApplicationCall.respondCacheConflictProblemDetails() = respondProblemDetails(
    DefaultProblemDetails(
        title = "cache-conflict",
        status = 409,
        detail = "Konflikt ved mellomlagring. Nøkkel eksisterer allerede.",
        instance = URI(request.path())
    ),
    logger = logger
)

private suspend fun ApplicationCall.respondCacheNotFoundProblemDetails() = respondProblemDetails(
    DefaultProblemDetails(
        title = "cache-ikke-funnet",
        status = 404,
        detail = "Cache ble ikke funnet.",
        instance = URI(request.path())
    ),
    logger = logger
)


private val logger = LoggerFactory.getLogger("no.nav.helse.mellomlagring.MellomlagringApisKt")

fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {
    route(MELLOMLAGRING_URL) {
        val mellomlagringPrefix = MellomlagringPrefix.SØKNAD

        post {
            val midlertidigSøknad = call.receive<Map<*, *>>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            try {
                mellomlagringService.setMellomlagring(
                    mellomlagringPrefix = mellomlagringPrefix,
                    verdi = JSONObject(midlertidigSøknad).toString(),
                    idToken = idToken,
                    callId = callId
                )
                call.respond(HttpStatusCode.Created)
            } catch (e: CacheConflictException) {
                call.respondCacheConflictProblemDetails()
            }
        }

        put {
            val midlertidigSøknad = call.receive<Map<*, *>>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            try {
                mellomlagringService.updateMellomlagring(
                    mellomlagringPrefix = mellomlagringPrefix,
                    idToken = idToken,
                    callId = callId,
                    verdi = JSONObject(midlertidigSøknad).toString()
                )
                call.respond(HttpStatusCode.NoContent)
            } catch (e: CacheNotFoundException) {
                call.respondCacheNotFoundProblemDetails()
            }
        }

        get {
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            val mellomlagring = mellomlagringService.getMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId
            )
            if (mellomlagring != null) {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = mellomlagring,
                    status = HttpStatusCode.OK
                )
            } else {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = "{}",
                    status = HttpStatusCode.OK
                )
            }
        }

        delete {
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.deleteMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId
            )
            call.respond(HttpStatusCode.Accepted)
        }
    }

    route(ENDRINGSMELDING_MELLOMLAGRING_URL) {
        val mellomlagringPrefix = MellomlagringPrefix.ENDRINGSMELDING

        post {

            val midlertidigEndringsmelding = call.receive<Map<*, *>>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            try {
                mellomlagringService.setMellomlagring(
                    mellomlagringPrefix = mellomlagringPrefix,
                    verdi = JSONObject(midlertidigEndringsmelding).toString(),
                    idToken = idToken,
                    callId = callId
                )
                call.respond(HttpStatusCode.Created)

            } catch (e: CacheConflictException) {
                call.respondCacheConflictProblemDetails()
            }
        }

        put {
            val midlertidigEndringsmelding = call.receive<Map<*, *>>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.updateMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId,
                verdi = JSONObject(midlertidigEndringsmelding).toString()
            )
            call.respond(HttpStatusCode.NoContent)
        }

        get {
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            val mellomlagring = mellomlagringService.getMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId
            )
            if (mellomlagring != null) {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = mellomlagring,
                    status = HttpStatusCode.OK
                )
            } else {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = "{}",
                    status = HttpStatusCode.OK
                )
            }
        }

        delete {
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.deleteMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId
            )
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
