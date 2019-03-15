package no.nav.helse

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.request.ApplicationRequest
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import java.lang.IllegalStateException

class MonitorReceivedHttpRequestsFeature (
    private val configure: Configuration
) {

    init {
        if (configure.app.isNullOrBlank()) {
            throw IllegalStateException("app må settes.")
        }
    }

    private val histogram = Histogram
        .build(
            "received_http_requests_histogram",
            "Histogram for alle HTTP-requester som treffer ${configure.app}")
        .labelNames("app", "verb", "path")
        .register()

    private val counter = Counter
        .build(
            "received_http_requests_counter",
            "Teller for alle HTTP-requester som treffer ${configure.app}")
        .labelNames("app", "verb", "path", "status")
        .register()

    class Configuration {
        var app : String? = null
        var skipPaths : List<String> = listOf("/isready", "/isalive", "/metrics", "/health")
        var overridePaths : Map<Regex, String> = mapOf()
    }

    private suspend fun intercept(context: PipelineContext<Unit, ApplicationCall>) {
        val verb = context.context.request.httpMethod.value
        val path = getPath(context.context.request)

        configure.overridePaths.forEach {
            path.matches(it.key)
        }

        if (!configure.skipPaths.contains(path)) {
            try {
                histogram.labels(configure.app, verb, path).startTimer().use {
                    context.proceed()
                }
            } finally {
                val httpStatusCode = (context.context.response.status() ?: HttpStatusCode.OK)
                val httpStatusCodeString = httpStatusCode.value.toString()
                val family = "${httpStatusCodeString[0]}xx"
                val success = if (httpStatusCode.isSuccess()) "success" else "failure"

                counter.labels(configure.app, verb, path, httpStatusCodeString).inc()
                counter.labels(configure.app, verb, path, family).inc()
                counter.labels(configure.app, verb, path, success).inc()
            }
        } else {
            context.proceed()
        }
    }

    private fun getPath(request: ApplicationRequest) : String {
        val path = request.path()
        configure.overridePaths.forEach {
            if (path.matches(it.key)) {
                return it.value
            }
        }
        return path
    }

    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, MonitorReceivedHttpRequestsFeature> {

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): MonitorReceivedHttpRequestsFeature {
            val result = MonitorReceivedHttpRequestsFeature(
                Configuration().apply(configure)
            )

            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.intercept(this)
            }
            return result
        }

        override val key = AttributeKey<MonitorReceivedHttpRequestsFeature>("MonitorReceivedHttpRequestsFeature")
    }
}