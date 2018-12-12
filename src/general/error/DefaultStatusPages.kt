package no.nav.pleiepenger.api.general.error

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.net.URI


fun StatusPages.Configuration.defaultStatusPages() {

    val logger: Logger = LoggerFactory.getLogger("defaultStatusPages")
    val aboutBlank = URI.create("about:blank")

    exception<IllegalStateException> { cause ->
        call.respond(HttpStatusCode.Forbidden, DefaultError(
            status = HttpStatusCode.Forbidden.value,
            type = aboutBlank,
            title = "Test"
        ))
        throw cause
    }

    exception<Throwable> { cause ->
        logger.info("something blew up", cause)
        call.respond(HttpStatusCode.InternalServerError, DefaultError(
            status = HttpStatusCode.InternalServerError.value,
            title = "Unhandled error"
        ))
        throw cause

    }
}