package no.nav.helse.vedlegg

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import no.nav.helse.general.CallId
import no.nav.helse.general.HttpRequest
import no.nav.helse.general.auth.IdToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

private val logger: Logger = LoggerFactory.getLogger("nav.PleiepengerDokumentGateway")


class PleiepengerDokumentGateway(
    private val httpClient : HttpClient,
    baseUrl : URL
) {

    private val url = HttpRequest.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "dokment")
    )

    suspend fun hentVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId) : Vedlegg? {

        val urlMedId = HttpRequest.buildURL(
            baseUrl = url,
            pathParts = listOf(vedleggId.value)
        )

        val httpRequest = httpRequest(
            idToken = idToken,
            callId = callId,
            url = urlMedId,
            httpMethod = HttpMethod.Get
        )

        val httpResponse = HttpRequest.monitored<HttpResponse>(
            httpClient = httpClient,
            httpRequest = httpRequest,
            expectedStatusCodes = listOf(HttpStatusCode.OK, HttpStatusCode.NotFound)
        )

        return if (httpResponse.status == HttpStatusCode.NotFound) null else {
            httpResponse.use { it ->
                it.receive<Vedlegg>()
            }
        }
    }

    suspend fun lagreVedlegg(
        vedlegg: Vedlegg,
        idToken: IdToken,
        callId: CallId) : VedleggId {

        val httpRequest = httpRequest(
            idToken = idToken,
            callId = callId,
            url = url,
            httpMethod = HttpMethod.Post
        ).body(vedlegg)

        val response = HttpRequest.monitored<CreatedResponseEntity>(
            httpClient = httpClient,
            httpRequest = httpRequest,
            expectedStatusCodes = listOf(HttpStatusCode.Created)
        )

        return VedleggId(response.id)
    }

    suspend fun slettVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId) : Boolean {

        val urlMedId = HttpRequest.buildURL(
            baseUrl = url,
            pathParts = listOf(vedleggId.value)
        )

        val httpRequest = httpRequest(
            idToken = idToken,
            callId = callId,
            url = urlMedId,
            httpMethod = HttpMethod.Delete
        )

        return try {
            HttpRequest.monitored<HttpResponse>(
                httpClient = httpClient,
                httpRequest = httpRequest,
                expectedStatusCodes = listOf(HttpStatusCode.NoContent)
            )
            true
        } catch (cause: Throwable) {
            false
        }
    }

    private fun httpRequest(
        idToken: IdToken,
        callId: CallId,
        url: URL,
        httpMethod: HttpMethod
    ) : HttpRequestBuilder {
        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, "Bearer ${idToken.value}")
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value)
        httpRequest.method = httpMethod
        httpRequest.url(url)
        return httpRequest
    }
}

private fun HttpRequestBuilder.body(vedlegg: Vedlegg): HttpRequestBuilder {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
    body = vedlegg
    return this
}

data class CreatedResponseEntity(val id : String)
