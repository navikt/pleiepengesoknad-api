package no.nav.helse.k9

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
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
import java.time.format.DateTimeFormatter

class K9OppslagGateway(
    private val baseUrl: URI,
    private val accessTokenClient: AccessTokenClient,
    private val apiGatewayApiKey: ApiGatewayApiKey,
    private val hentePersonInfoScopes : Set<String> = setOf("openid")
) : HealthCheck {

    private companion object {
        private const val CORRELATION_ID_HEADER = "Nav-Call-Id"
        private val logger: Logger = LoggerFactory.getLogger("nav.K9OppslagGateway")
        private const val HENTE_SOKER_OPERATION = "hente-soker"
        private const val HENTE_BARN_OPERATION = "hente-barn"
        private const val HENTE_ARBEIDSGIVERE_OPERATION = "hente-arbeidsgivere"
        private val objectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
        }
    }

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)


    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(hentePersonInfoScopes)
            Healthy("K9OppslagGateway", "Henting av access token for henting av personinformasjon OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for henting av personinformasjon", cause)
            UnHealthy("K9OppslagGateway", "Henting av access token for henting av personinformasjon feilet.")
        }
    }

    private fun generateHttpRequest(
        url: String,
        personIdent: String,
        callId: CallId
    ): Request {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(hentePersonInfoScopes).asAuthoriationHeader()
        return url
            .httpGet()
            .header(
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.Accept to "application/json",
                "Nav-Consumer-Id" to "pleiepengesoknad-api",
                "Nav-Personidenter" to personIdent,
                CORRELATION_ID_HEADER to callId.value,
                apiGatewayApiKey.headerKey to apiGatewayApiKey.value
            )
    }

/*
    suspend fun hentAktoerId(
        personIdent: String,
        callId: CallId
    ) : AktoerId {
        val httpRequest = generateHttpRequest(aktoerIdUrl, personIdent, callId)

        val httpResponse = Retry.retry(
            operation = HENTE_AKTOER_ID_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_AKTOER_ID_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }
            result.fold(
                { success -> objectMapper.readValue<Map<String, AktoerRegisterIdentResponse>>(success) },
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av Aktør ID.")
                }
            )
        }
        if (!httpResponse.containsKey(personIdent)) {
            throw IllegalStateException("Svar fra '$aktoerIdUrl' inneholdt ikke data om det forsespurte fødselsnummeret.")
        }

        val identResponse =  httpResponse.get(key = personIdent)

        if (identResponse!!.feilmelding!= null) {
            logger.warn("Mottok feilmelding fra AktørRegister : '${identResponse.feilmelding}'")
        }

        if (identResponse.identer == null) {
            throw IllegalStateException("Fikk 0 AktørID'er for det forsespurte fødselsnummeret mot '$aktoerIdUrl'")
        }

        if (identResponse.identer.size != 1) {
            throw IllegalStateException("Fikk ${identResponse.identer.size} AktørID'er for det forsespurte fødselsnummeret mot '$aktoerIdUrl'")
        }

        val aktoerId = AktoerId(identResponse.identer[0].ident)
        logger.trace("Resolved AktørID $aktoerId")
        return AktoerId(aktoerId.value)
    }
*/
    suspend fun hentSoker(
        personIdent: String,
        callId : CallId
    ) : SokerOppslagRespons {
        val sokerUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                Pair("a", listOf("aktør_id", "fornavn", "mellomnavn", "etternavn", "fødselsdato"))
            )
        ).toString()
        val httpRequest = generateHttpRequest(sokerUrl, personIdent, callId)

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

    suspend fun hentBarn(
        personIdent: String,
        callId : CallId
    ) : List<Barn> {
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

    data class BarnOppslagResponse(val barn: List<Barn>)
}