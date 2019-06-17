package no.nav.helse.soknad

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.general.CallId
import no.nav.helse.general.systemauth.AuthorizationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI

class PleiepengesoknadProsesseringGateway(
    baseUrl : URI,
    private val authorizationService: AuthorizationService
){

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(PleiepengesoknadProsesseringGateway::class.java)
        private val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
    }

    private val komplettUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "soknad")
    ).toString()

    suspend fun leggTilProsessering(
        soknad : KomplettSoknad,
        callId: CallId
    ) {
        val authorizationHeader = authorizationService.getAuthorizationHeader()
        val body = objectMapper.writeValueAsBytes(soknad)

        val httpRequet = komplettUrl
            .httpPost()
            .timeout(20_000)
            .timeoutRead(20_000)
            .body( {ByteArrayInputStream(body)} )
            .header(
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.Authorization to authorizationHeader
            )

        val (request, _, result) = Operation.monitored(
            app = "pleiepengesoknad-api",
            operation = "sende-soknad-til-prosessering"
        ) { httpRequet.awaitStringResponseResult() }

        result.fold(
            { success -> },
            { error ->
                logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                logger.error(error.toString())
                throw IllegalStateException("Feil ved sending av søknad til prosessering.")
            }
        )
    }
}