package no.nav.helse.mellomlagring

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation.Companion.monitored
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.general.CallId
import no.nav.helse.k9MellomlagringKonfigurert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.ZonedDateTime
import kotlin.reflect.jvm.jvmName

class K9BrukerdialogCacheGateway(
    private val tokenxClient: AccessTokenClient,
    private val k9BrukerdialogCacheTokenxAudience: Set<String>,
    baseUrl: URI
) : HealthCheck {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9BrukerdialogCacheGateway::class.java)
        private val objectMapper = jacksonObjectMapper().k9MellomlagringKonfigurert()
        private const val LAGRE_CACHE_OPERATION = "lagre-cache"
        private const val HENTE_CACHE_OPERATION = "hente-cache"
        private const val OPPDATERE_CACHE_OPERATION = "oppdatere-cache"
        private const val SLETTE_CACHE_OPERATION = "slette-cache"
        private const val TJENESTE = "pleiepengesoknad-api"
    }

    private val cachedAccessTokenClient = CachedAccessTokenClient(tokenxClient)
    private val komplettUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api", "cache")
    )

    override suspend fun check(): Result {
        val k9BrukerdialogCache = K9BrukerdialogCacheGateway::class.jvmName
        return try {
            tokenxClient.getAccessToken(k9BrukerdialogCacheTokenxAudience)
            Healthy(k9BrukerdialogCache, "Henting av access token for $k9BrukerdialogCacheTokenxAudience.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for $k9BrukerdialogCacheTokenxAudience", cause)
            UnHealthy(k9BrukerdialogCache, "Henting av access token for $k9BrukerdialogCacheTokenxAudience.")
        }
    }

    suspend fun mellomlagreSøknad(
        cacheRequestDTO: CacheRequestDTO,
        idToken: IdToken,
        callId: CallId
    ): CacheResponseDTO? {
        val body = objectMapper.writeValueAsBytes(cacheRequestDTO)

        val exchangeToken = cachedAccessTokenClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = komplettUrl
            .toString()
            .httpPost()
            .body(body)
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = LAGRE_CACHE_OPERATION,
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved mellomlagring av søknad")
                objectMapper.readValue<CacheResponseDTO>(success)
            },
            { error ->
                if (409 == response.statusCode) throw CacheConflictException(cacheRequestDTO.nøkkelPrefiks)
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av mellomlagret søknad.")
                }
            }
        )
    }

    suspend fun hentMellomlagretSøknad(nøkkelPrefiks: String, idToken: IdToken, callId: CallId): CacheResponseDTO? {
        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(nøkkelPrefiks)
        )

        val exchangeToken = cachedAccessTokenClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = urlMedId
            .toString()
            .httpGet()
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = HENTE_CACHE_OPERATION,
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved henting av mellomlagret søknad")
                objectMapper.readValue<CacheResponseDTO>(success)
            },
            { error ->
                if (404 == response.statusCode) null
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av mellomlagret søknad.")
                }
            }
        )
    }


    suspend fun oppdaterMellomlagretSøknad(
        cacheRequestDTO: CacheRequestDTO,
        idToken: IdToken,
        callId: CallId
    ): CacheResponseDTO {
        val body = objectMapper.writeValueAsBytes(cacheRequestDTO)

        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(cacheRequestDTO.nøkkelPrefiks)
        )

        val exchangeToken = cachedAccessTokenClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = urlMedId
            .toString()
            .httpPut()
            .body(body)
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = OPPDATERE_CACHE_OPERATION,
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved mellomlagring av søknad")
                objectMapper.readValue<CacheResponseDTO>(success)
            },
            { error ->
                if (response.statusCode == 404) throw CacheNotFoundException(cacheRequestDTO.nøkkelPrefiks)
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av mellomlagret søknad.")
                }
            }
        )
    }

    suspend fun slettMellomlagretSøknad(nøkkelPrefiks: String, idToken: IdToken, callId: CallId): Boolean {
        val urlMedId = Url.buildURL(
            baseUrl = komplettUrl,
            pathParts = listOf(nøkkelPrefiks)
        )

        val exchangeToken = cachedAccessTokenClient.getAccessToken(k9BrukerdialogCacheTokenxAudience, idToken.value)

        val httpRequest = urlMedId
            .toString()
            .httpDelete()
            .header(
                HttpHeaders.Authorization to "Bearer ${exchangeToken.token}",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.Accept to "application/json"
            )

        val (request, response, result) = monitored(
            app = TJENESTE,
            operation = HENTE_CACHE_OPERATION,
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { success ->
                logger.info("Suksess ved sletting av mellomlagret søknad")
                true
            },
            { error ->
                if (404 == response.statusCode) throw CacheNotFoundException(nøkkelPrefiks)
                else {
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved sletting av mellomlagret søknad.")
                }
            }
        )
    }
}

data class CacheRequestDTO(
    val nøkkelPrefiks: String,
    val verdi: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val endret: ZonedDateTime? = null
)

data class CacheResponseDTO(
    val nøkkel: String,
    val verdi: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val endret: ZonedDateTime? = null
)

class CacheConflictException(nøkkelPrefiks: String) :
    RuntimeException("Cache med nøkkelPrefiks = $nøkkelPrefiks finnes allerede for person.")

class CacheNotFoundException(nøkkelPrefiks: String) :
    RuntimeException("Cache med nøkkelPrefiks = $nøkkelPrefiks for person ble ikke funnet.")
