package no.nav.helse.general.error

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun StatusPages.Configuration.defaultStatusPages() {

    val logger: Logger = LoggerFactory.getLogger("nav.defaultStatusPages")

    exception<Throwable> { cause ->
        logger.info("something blew up", cause)
        call.respond(HttpStatusCode.InternalServerError, DefaultError(
            status = HttpStatusCode.InternalServerError.value,
            title = "Unhandled error."
        ))
        throw cause

    }
}