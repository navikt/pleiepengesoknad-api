package no.nav.helse.soker

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import io.ktor.http.Url
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.oppslag.K9OppslagGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class SokerGateway (
    baseUrl: URI,
    accessTokenClient: AccessTokenClient,
    apiGatewayApiKey: ApiGatewayApiKey
) : K9OppslagGateway(baseUrl, accessTokenClient, apiGatewayApiKey) {

    protected companion object {
        protected val logger: Logger = LoggerFactory.getLogger("nav.SokerGateway")
        protected const val HENTE_SOKER_OPERATION = "hente-soker"
        protected val objectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
        }
    }

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(scopes)
            Healthy("ArbeidsgivereGateway", "Henting av access token for henting av arbeidsgivere OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for henting av arbeidsgivere", cause)
            UnHealthy("ArbeidsgivereGateway", "Henting av access token for henting av arbeidsgivere feilet.")
        }
    }

    suspend fun hentSoker(
        ident: NorskIdent,
        callId : CallId
    ) : SokerOppslagRespons {
        val sokerUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                Pair("a", listOf("aktør_id", "fornavn", "mellomnavn", "etternavn", "fødselsdato"))
            )
        ).toString()
        val httpRequest = generateHttpRequest(sokerUrl, ident, callId)

        val oppslagRespons = Retry.retry(
            operation = HENTE_SOKER_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_SOKER_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<SokerOppslagRespons>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av søkers personinformasjon")
                }
            )
        }
        return oppslagRespons
    }
    data class SokerOppslagRespons(
        val aktør_id: String,
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val fødselsdato: LocalDate
    )
}