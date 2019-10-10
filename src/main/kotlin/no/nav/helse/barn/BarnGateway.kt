package no.nav.helse.barn

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import io.ktor.http.Url
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

class BarnGateway (
    baseUrl: URI,
    accessTokenClient: AccessTokenClient,
    apiGatewayApiKey: ApiGatewayApiKey
    ) : K9OppslagGateway(baseUrl, accessTokenClient, apiGatewayApiKey) {

    protected companion object {
        protected val logger: Logger = LoggerFactory.getLogger("nav.BarnGateway")
        protected const val HENTE_BARN_OPERATION = "hente-barn"
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

    suspend fun hentBarn(
        personIdent: String,
        callId : CallId
    ) : List<BarnOppslagDTO> {
        val barnUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                Pair("a", listOf("barn[].aktør_id",
                    "barn[].fornavn",
                    "barn[].mellomnavn",
                    "barn[].etternavn",
                    "barn[].fødselsdato")
                )
            )
        ).toString()

        val httpRequest = generateHttpRequest(barnUrl, personIdent, callId)

        val oppslagRespons = Retry.retry(
            operation = HENTE_BARN_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_BARN_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<BarnOppslagResponse>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av informasjon om søkers barn")
                }
            )
        }
        return oppslagRespons.barn
    }

    private data class BarnOppslagResponse(val barn: List<BarnOppslagDTO>)

    data class BarnOppslagDTO (
        val fødselsdato: LocalDate,
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String,
        val aktør_id: String
    )
}