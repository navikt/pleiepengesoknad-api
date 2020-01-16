package no.nav.helse.mellomlagring

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.delete
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/mellomlagring")
    class mellomlagring

    post { _: mellomlagring ->
        val midlertidigSøknad = call.receive<String>()
        val idToken = idTokenProvider.getIdToken(call)
        mellomlagringService.setMellomlagring(idToken, call.getCallId(), midlertidigSøknad)
        call.respond(HttpStatusCode.Accepted)
    }

    get { _: mellomlagring ->
        val idToken = idTokenProvider.getIdToken(call)
        mellomlagringService.getMellomlagring(idToken, call.getCallId())
        call.respond(HttpStatusCode.Accepted)
    }

    delete { _: mellomlagring ->
        val idToken = idTokenProvider.getIdToken(call)
        mellomlagringService.deleteMellomlagring(idToken, call.getCallId())
        call.respond(HttpStatusCode.Accepted)
    }
}