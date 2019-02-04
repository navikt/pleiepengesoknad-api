package no.nav.helse.general.error

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.prometheus.client.Counter
import java.net.URI

fun StatusPages.Configuration.defaultStatusPages(
    errorCounter: Counter
) {

    exception<Throwable> { cause ->
        cause.getRootCause()
        val error = DefaultError(
            status = HttpStatusCode.InternalServerError.value,
            title = "Unhandled error."
        )
        monitorException(cause, error.type, errorCounter)
        call.respond(HttpStatusCode.InternalServerError, error)
        throw cause

    }
}

fun Throwable.getRootCause() : Throwable {
    var rootCause= this

    while (rootCause.cause != null && rootCause.cause != rootCause) {
        rootCause = rootCause.cause!!
    }

    return rootCause
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

