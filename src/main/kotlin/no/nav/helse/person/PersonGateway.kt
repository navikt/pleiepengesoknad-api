package no.nav.helse.person

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import no.nav.helse.aktoer.AktoerId
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.ApiGatewayApiKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class PersonGateway(
    private val baseUrl: URI,
    private val accessTokenClient: AccessTokenClient,
    private val henteBarnScopes : Set<String> = setOf("openid"),
    private val apiGatewayApiKey: ApiGatewayApiKey
) : HealthCheck {

    private companion object {
        private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"
        private val logger: Logger = LoggerFactory.getLogger("nav.PersonGateway")
        private const val HENTE_PERSON_OPERATION = "hente-person"
        private val objectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
        }
    }

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(henteBarnScopes)
            Healthy("PersonGateway", "Henting av access token for henting av person OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for henting av person", cause)
            UnHealthy("PersonGateway", "Henting av access token for henting av person feilet.")
        }
    }

    suspend fun hentPerson(
        aktoerId: AktoerId,
        callId : CallId
    ) : Person {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(henteBarnScopes).asAuthoriationHeader()

        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "person",
                aktoerId.value
            )
        )


        val httpRequest = url
            .toString()
            .httpGet()
            .header(
                SPARKEL_CORRELATION_ID_HEADER to callId.value,
                HttpHeaders.Accept to "application/json",
                HttpHeaders.Authorization to authorizationHeader,
                apiGatewayApiKey.headerKey to apiGatewayApiKey.value
            )


        val sparkelResponse = Retry.retry(
            operation = HENTE_PERSON_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_PERSON_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<SparkelResponse>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av person")
                }
            )
        }

        return Person(
            fornavn = sparkelResponse.fornavn,
            mellomnavn = sparkelResponse.mellomnavn,
            etternavn = sparkelResponse.etternavn,
            fodselsdato = sparkelResponse.fdato
        )
    }
}

data class SparkelResponse(val fornavn: String, val mellomnavn: String?, val etternavn: String, val fdato: LocalDate)