package no.nav.helse.soknad

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.general.CallId
import no.nav.helse.general.HttpRequest
import java.net.URL

class PleiepengesoknadProsesseringGateway(
    private val monitoredHttpClient: MonitoredHttpClient,
    baseUrl : URL,
    private val systemCredentialsProvider: SystemCredentialsProvider
){

    private val komplettUrl = HttpRequest.buildURL(
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

        monitoredHttpClient.request(
            httpRequestBuilder = httpRequest,
            expectedHttpResponseCodes = setOf(HttpStatusCode.Accepted)
        ).use {}
    }
}