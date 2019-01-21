package no.nav.helse.general

import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.error.CommunicationException
import no.nav.helse.id.Id
import no.nav.helse.id.IdService
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
                              url : URL) : HttpRequestBuilder {
    val httpRequestBuilder = HttpRequestBuilder()
    httpRequestBuilder.header("Authorization", authorization)
    //httpRequestBuilder.header("Accept", "application/json")
    httpRequestBuilder.url(url)
    return httpRequestBuilder
}

suspend inline fun <reified T> lookupThroughSparkel(
    httpClient: HttpClient,
    idService: IdService,
    tokenProvider: ServiceAccountTokenProvider,
    url: URL,
    fnr: Fodselsnummer) : T {

    try {
        return sparkelRequest(
            httpClient = httpClient,
            id = idService.getId(fnr),
            tokenProvider = tokenProvider,
            url = url
        )
    } catch (cause: BadResponseStatusException) {
        if (HttpStatusCode.NotFound == cause.statusCode) {
            logger.info("Got 404 response looking up through sparkel, getting new ID and retrying")
            return sparkelRequest(
                httpClient = httpClient,
                id = idService.refreshAndGetId(fnr),
                tokenProvider = tokenProvider,
                url = url
            )
        } else {
            throw CommunicationException(url, cause.response)
        }
    } catch (cause: Throwable) {
        throw CommunicationException(url, cause)
    }
}

suspend inline fun <reified T> sparkelRequest(
    httpClient: HttpClient,
    id: Id,
    tokenProvider: ServiceAccountTokenProvider,
    url: URL) : T {
    return httpClient.get(
        prepareHttpRequestBuilder(
            url = buildURL(url, queryParameters = mapOf(Pair("uuid", id.value))),
            authorization = tokenProvider.getAuthorizationHeader()
        )
    )
}