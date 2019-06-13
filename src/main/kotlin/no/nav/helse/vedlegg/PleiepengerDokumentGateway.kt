package no.nav.helse.vedlegg

import io.ktor.client.call.receive
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.time.Duration

private val logger: Logger = LoggerFactory.getLogger("nav.PleiepengerDokumentGateway")


class PleiepengerDokumentGateway(
    baseUrl : URI
) {

    private val url = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "dokument")
    )

    suspend fun hentVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId) : Vedlegg? {

        val urlMedId = Url.buildURL(
            baseUrl = url,
            pathParts = listOf(vedleggId.value)
        )

        val httpRequest = httpRequest(
            idToken = idToken,
            callId = callId,
            url = urlMedId,
            httpMethod = HttpMethod.Get
        )

        val response = Retry.retry(
            operation = "hente-vedlegg",
            tries = 3,
            initialDelay = Duration.ofMillis(100),
            maxDelay = Duration.ofMillis(300),
            logger = logger
        ) {
            monitoredHttpClient.request(
                httpRequestBuilder = HttpRequestBuilder().takeFrom(httpRequest),
                expectedHttpResponseCodes = setOf(HttpStatusCode.OK, HttpStatusCode.NotFound)
            )
        }

        return response.use {
            if (it.status == HttpStatusCode.NotFound) null
            else it.receive()
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

        val response = Retry.retry(
            operation = "lagre-vedlegg",
            tries = 3,
            initialDelay = Duration.ofMillis(100),
            maxDelay = Duration.ofMillis(300),
            logger = logger
        ) {
            monitoredHttpClient.requestAndReceive<CreatedResponseEntity>(
                httpRequestBuilder = HttpRequestBuilder().takeFrom(httpRequest),
                expectedHttpResponseCodes = setOf(HttpStatusCode.Created)
            )
        }

        return VedleggId(response.id)
    }

    suspend fun slettVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId) : Boolean {

        val urlMedId = Url.buildURL(
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
            Retry.retry(
                operation = "slett-vedlegg",
                tries = 3,
                initialDelay = Duration.ofMillis(100),
                maxDelay = Duration.ofMillis(300),
                logger = logger
            ) {
                monitoredHttpClient.request(
                    httpRequestBuilder = HttpRequestBuilder().takeFrom(httpRequest),
                    expectedHttpResponseCodes = setOf(HttpStatusCode.NoContent)
                ).use {}
            }
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
