package no.nav.helse.soknad

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration

private val logger: Logger = LoggerFactory.getLogger("nav.PleiepengesoknadProsesseringGateway")


class PleiepengesoknadProsesseringGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    baseUrl : URL,
    private val systemCredentialsProvider: SystemCredentialsProvider
){

    private val komplettUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "soknad")
    )

    suspend fun leggTilProsessering(
        soknad : KomplettSoknad,
        callId: CallId
    ) {
        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemCredentialsProvider.getAuthorizationHeader())
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value)
        httpRequest.header(HttpHeaders.ContentType, ContentType.Application.Json)
        httpRequest.method = HttpMethod.Post
        httpRequest.body = soknad
        httpRequest.url(komplettUrl)

        request(httpRequest)
    }

    private suspend fun request(httpRequest: HttpRequestBuilder) {
        Retry.retry(
            operation = "send-soknad",
            tries = 3,
            initialDelay = Duration.ofMillis(100),
            maxDelay = Duration.ofMillis(300),
            logger = logger
        ) {
            monitoredHttpClient.request(
                httpRequestBuilder = HttpRequestBuilder().takeFrom(httpRequest),
                expectedHttpResponseCodes = setOf(HttpStatusCode.Accepted)
            ).use {}
        }
    }
}