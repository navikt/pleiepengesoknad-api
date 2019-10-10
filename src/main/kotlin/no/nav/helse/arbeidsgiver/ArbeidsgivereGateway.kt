package no.nav.helse.arbeidsgiver

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
import java.time.format.DateTimeFormatter

class ArbeidsgivereGateway(
    baseUrl: URI,
    accessTokenClient: AccessTokenClient,
    apiGatewayApiKey: ApiGatewayApiKey
) : K9OppslagGateway(baseUrl, accessTokenClient, apiGatewayApiKey) {

    protected companion object {
        protected val logger: Logger = LoggerFactory.getLogger("nav.ArbeidsgivereGateway")
        protected const val HENTE_ARBEIDSGIVERE_OPERATION = "hente-arbeidsgivere"
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

    internal suspend fun hentArbeidsgivere(
        personIdent: String,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Organisasjon> {
        val arbeidsgivereUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                Pair("a", listOf( "arbeidsgivere[].organisasjoner[].organisasjonsnummer",
                    "arbeidsgivere[].organisasjoner[].navn")),
                Pair("fom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))),
                Pair("tom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed)))
            )
        ).toString()

        val httpRequest = generateHttpRequest(arbeidsgivereUrl, personIdent, callId)

        val arbeidsgivere = Retry.retry(
            operation = HENTE_ARBEIDSGIVERE_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_ARBEIDSGIVERE_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<Arbeidsgivere>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av arbeidsgiver.")
                }
            )
        }
        return arbeidsgivere.organisasjoner
    }
}