package no.nav.helse.general

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import no.nav.helse.general.auth.ApiGatewayApiKey
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
        .trimmedPath(withBasePath)

    val url = urlBuilder.build().toURI().toURL()
    logger.info("Built URL '$url'")
    return url
}


fun prepareHttpRequestBuilder(authorization : String? = null,
                              url : URL,
                              callId: CallId? = null,
                              apiGatewayApiKey: ApiGatewayApiKey? = null,
                              httpRequestBuilder: HttpRequestBuilder = HttpRequestBuilder()) : HttpRequestBuilder {
    if (authorization != null) {
        httpRequestBuilder.header("Authorization", authorization)
    }
    if (callId != null) {
        httpRequestBuilder.header("Nav-Call-Id", callId.value)
    }
    if (apiGatewayApiKey != null) {
        httpRequestBuilder.header("x-nav-apiKey", apiGatewayApiKey.value)
    }
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

private fun URLBuilder.trimmedPath(pathParts : List<String>): URLBuilder  {
    val trimmedPathParts = mutableListOf<String>()
    pathParts.forEach { part ->
        if (part.isNotBlank()) {
            trimmedPathParts.add(part.trimStart('/').trimEnd('/'))
        }
    }
    return path(trimmedPathParts)
}