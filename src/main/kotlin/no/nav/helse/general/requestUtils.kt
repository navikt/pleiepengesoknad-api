package no.nav.helse.general

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL


val logger: Logger = LoggerFactory.getLogger("nav.requestUtils")

fun buildURL(
    baseUrl: URL,
    pathParts: List<String> = listOf(),
    queryParameters: Map<String, String> = mapOf()
): URL {
    val withBasePath= mutableListOf(baseUrl.path)
    withBasePath.addAll(pathParts)

    val parametersBuilder = ParametersBuilder()
    queryParameters.forEach { queryParameter ->
        parametersBuilder.append(queryParameter.key, queryParameter.value)
    }

    val urlBuilder = URLBuilder(
        parameters = parametersBuilder
    )
        .takeFrom(baseUrl.toString())
        .path(withBasePath)

    val url = urlBuilder.build().toURI().toURL()
    logger.info("Built URL '$url'")
    return url
}


fun prepareHttpRequestBuilder(authorization : String,
                              url : URL,
                              httpRequestBuilder: HttpRequestBuilder = HttpRequestBuilder()) : HttpRequestBuilder {
    httpRequestBuilder.header("Authorization", authorization)
    httpRequestBuilder.url(url)
    return httpRequestBuilder
}

suspend fun <T>monitoredOperation(operation: suspend () -> T,
                                  histogram: Histogram,
                                  counter: Counter,
                                  highResponseTime: Double = 0.4) : T {
    val timer = histogram.startTimer()
    var label = "error"
    try {
        val result = operation.invoke()
        label = "success"
        return result
    } finally {
        val elapsed = timer.observeDuration()
        if (elapsed > highResponseTime) {
            counter.labels(label, "slow").inc()
        } else {
            counter.labels(label, "acceptable").inc()
        }
    }
}

fun monitoredOperationtCounter(name: String, help: String) : Counter {
    return Counter.build()
        .name(name)
        .help(help)
        .labelNames("request_status", "response_time_status")
        .register()
}