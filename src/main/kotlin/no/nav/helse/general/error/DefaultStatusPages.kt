package no.nav.helse.general.error

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.prometheus.client.Counter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

fun StatusPages.Configuration.defaultStatusPages(
    errorCounter: Counter
) {

    val logger: Logger = LoggerFactory.getLogger("nav.defaultStatusPages")

    exception<Throwable> { cause ->
        logger.info("something blew up", cause)
        val error = DefaultError(
            status = HttpStatusCode.InternalServerError.value,
            title = "Unhandled error."
        )
        monitorException(cause, error.type, errorCounter)
        call.respond(HttpStatusCode.InternalServerError, error)
        throw cause

    }
}

internal fun initializeErrorCounter() : Counter {
    return Counter.build()
        .name("counter_antall_feilsituasjoner")
        .help("Antall feilsituasjoner oppstått")
        .labelNames("error_type", "exception_name")
        .register()
}

internal fun monitorException(cause: Throwable,
                     type: URI,
                     counter: Counter) {
    counter.labels(type.toString(), cause.javaClass.simpleName).inc()
}

