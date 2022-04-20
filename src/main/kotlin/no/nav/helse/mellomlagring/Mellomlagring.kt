package no.nav.helse.mellomlagring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.ENDRINGSMELDING_MELLOMLAGRING_URL
import no.nav.helse.MELLOMLAGRING_URL
import no.nav.helse.general.auth.IdTokenProvider

fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {
    route(MELLOMLAGRING_URL) {
        val mellomlagringPrefix = MellomlagringPrefix.SØKNAD

        post {
            val midlertidigSøknad = call.receive<String>()
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.setMellomlagring(mellomlagringPrefix, idToken.getSubject()!!, midlertidigSøknad)
            call.respond(HttpStatusCode.NoContent)
        }

        put {
            val midlertidigSøknad = call.receive<String>()
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.updateMellomlagring(mellomlagringPrefix, idToken.getSubject()!!, midlertidigSøknad)
            call.respond(HttpStatusCode.NoContent)
        }

        get {
            val idToken = idTokenProvider.getIdToken(call)
            val mellomlagring = mellomlagringService.getMellomlagring(mellomlagringPrefix, idToken.getSubject()!!)
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
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.deleteMellomlagring(mellomlagringPrefix, idToken.getSubject()!!)
            call.respond(HttpStatusCode.Accepted)
        }
    }

    route(ENDRINGSMELDING_MELLOMLAGRING_URL) {
        val mellomlagringPrefix = MellomlagringPrefix.ENDRINGSMELDING

        post {

            val midlertidigEndringsmelding = call.receive<String>()
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.setMellomlagring(mellomlagringPrefix, idToken.getSubject()!!, midlertidigEndringsmelding)
            call.respond(HttpStatusCode.NoContent)
        }

        put {
            val midlertidigEndringsmelding = call.receive<String>()
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.updateMellomlagring(mellomlagringPrefix, idToken.getSubject()!!, midlertidigEndringsmelding)
            call.respond(HttpStatusCode.NoContent)
        }

        get {
            val idToken = idTokenProvider.getIdToken(call)
            val mellomlagring = mellomlagringService.getMellomlagring(mellomlagringPrefix, idToken.getSubject()!!)
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
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.deleteMellomlagring(mellomlagringPrefix, idToken.getSubject()!!)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}