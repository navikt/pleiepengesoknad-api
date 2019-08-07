package no.nav.helse.arbeidsgiver

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.general.*
import no.nav.helse.general.auth.ApiGatewayApiKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ArbeidsgiverGateway(
    private val baseUrl: URI,
    private val aktoerService: AktoerService,
    private val accessTokenClient: AccessTokenClient,
    private val henteArbeidsgivereScopes : Set<String> = setOf("openid"),
    private val apiGatewayApiKey: ApiGatewayApiKey
) : HealthCheck {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidsgiverGateway::class.java)
        private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"
        private const val HENTE_ARBEIDSGIVERE_OPERATION = "hente-arbeidsgivere"
        private val objectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(henteArbeidsgivereScopes)
            Healthy("ArbeidsgiverGateway", "Henting av access token for henting av arbeidsgivere OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for henting av arbeidsgivere", cause)
            UnHealthy("ArbeidsgiverGateway", "Henting av access token for henting av arbeidsgivere feilet.")
        }
    }

    internal suspend fun getAnsettelsesforhold(
        norskIdent: NorskIdent,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : List<Arbeidsgiver> {
        val sparkelResponse = try { request(norskIdent, callId, fraOgMed, tilOgMed) } catch (cause: Throwable) {
            logger.error("Feil ved oppslag på arbeidsgivere. Returnerer tom liste med arbeidsgivere.", cause)
            SparkelResponse(arbeidsgivere = setOf())
        }

        return sparkelResponse.arbeidsgivere.map { arbeidsforhold ->
            Arbeidsgiver(
                navn = arbeidsforhold.navn,
                organisasjonsnummer = arbeidsforhold.orgnummer
            )
        }
    }
    
    private suspend fun request(
        norskIdent: NorskIdent,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : SparkelResponse {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(henteArbeidsgivereScopes).asAuthoriationHeader()

        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "arbeidsgivere",
                aktoerService.getAktorId(norskIdent, callId).value
            ),
            queryParameters = mapOf(
                Pair("fom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))),
                Pair("tom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed)))
            )
        )

        val httpReqeust = url
            .toString()
            .httpGet()
            .header(
                SPARKEL_CORRELATION_ID_HEADER to callId.value,
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.Accept to "application/json",
                apiGatewayApiKey.headerKey to apiGatewayApiKey.value
            )

        return Retry.retry(
            operation = HENTE_ARBEIDSGIVERE_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_ARBEIDSGIVERE_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpReqeust.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<SparkelResponse>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av arbeidsgiver.")
                }
            )
        }
    }
}

data class SparkelArbeidsforhold(val orgnummer: String, val navn: String?)
data class SparkelResponse(val arbeidsgivere: Set<SparkelArbeidsforhold>)