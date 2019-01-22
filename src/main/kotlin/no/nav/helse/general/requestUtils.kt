package no.nav.helse.general

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
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