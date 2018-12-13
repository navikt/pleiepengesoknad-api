package no.nav.pleiepenger.api.general.auth

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import no.nav.pleiepenger.api.general.error.DefaultError
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun StatusPages.Configuration.authorizationStatusPages() {

    val logger: Logger = LoggerFactory.getLogger("nav.authorizationStatusPages")

    exception<UnauthorizedException> { cause ->
        call.respond(HttpStatusCode.Forbidden, DefaultError(
            status = HttpStatusCode.Forbidden.value,
            title = "authorizationStatusPages"
        ))
    }


}