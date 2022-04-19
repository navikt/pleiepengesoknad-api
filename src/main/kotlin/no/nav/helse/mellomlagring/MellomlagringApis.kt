package no.nav.helse.mellomlagring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.ENDRINGSMELDING_MELLOMLAGRING_URL
import no.nav.helse.MELLOMLAGRING_URL
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.soknad.hentIdTokenOgCallId

fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {
    route(MELLOMLAGRING_URL) {
        val mellomlagringPrefix = MellomlagringPrefix.SØKNAD

        post {
            val midlertidigSøknad = call.receive<String>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.setMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                verdi = midlertidigSøknad,
                idToken = idToken,
                callId = callId
            )
            call.respond(HttpStatusCode.Created)
        }

        put {
            val midlertidigSøknad = call.receive<String>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.updateMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId,
                verdi = midlertidigSøknad
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

    route(ENDRINGSMELDING_MELLOMLAGRING_URL) {
        val mellomlagringPrefix = MellomlagringPrefix.ENDRINGSMELDING

        post {

            val midlertidigEndringsmelding = call.receive<String>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.setMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                verdi = midlertidigEndringsmelding,
                idToken = idToken,
                callId = callId
            )
            call.respond(HttpStatusCode.Created)
        }

        put {
            val midlertidigEndringsmelding = call.receive<String>()
            val (idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
            mellomlagringService.updateMellomlagring(
                mellomlagringPrefix = mellomlagringPrefix,
                idToken = idToken,
                callId = callId,
                verdi = midlertidigEndringsmelding
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
