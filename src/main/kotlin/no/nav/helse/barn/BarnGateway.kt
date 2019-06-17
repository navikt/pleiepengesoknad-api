package no.nav.helse.barn

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import no.nav.helse.aktoer.AktoerId
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.general.CallId
import no.nav.helse.general.systemauth.AuthorizationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class BarnGateway(
    private val baseUrl: URI,
    private val aktoerService: AktoerService,
    private val authorizationService: AuthorizationService
) {

    private companion object {
        private const val SPARKEL_CORRELATION_ID_HEADER = "Nav-Call-Id"
        private const val HENTE_BARN_OPEARTION = "hente-barn"
        private val logger: Logger = LoggerFactory.getLogger(BarnGateway::class.java)
        private val objectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
        }
    }

    internal suspend fun hentBarn(
        norskIdent: NorskIdent,
        callId : CallId
    ) : List<Barn> {

        val authorizationHeader = authorizationService.getAuthorizationHeader()

        val url = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "person",
                aktoerService.getAktorId(norskIdent, callId).value,
                "barn"
            )
        )

        val httpRequest = url
            .toString()
            .httpGet()
            .header(
                SPARKEL_CORRELATION_ID_HEADER to callId.value,
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.Accept to "application/json"
            )

        val response = request(httpRequest)

        return response.barn.map { sparkelBarn ->
            Barn(
                aktoerId = AktoerId(sparkelBarn.aktørId),
                fornavn = sparkelBarn.fornavn,
                mellomnavn = sparkelBarn.mellomnavn,
                etternavn = sparkelBarn.etternavn,
                fodselsdato = sparkelBarn.fdato,
                status = sparkelBarn.status,
                diskresjonskode = sparkelBarn.diskresjonskode
            )
        }
    }

    private suspend fun request(
        httpRequest: Request
    ) : SparkelResponse {
        return Retry.retry(
            operation = HENTE_BARN_OPEARTION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_BARN_OPEARTION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<SparkelResponse>(success) },
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av barn")
                }
            )
        }
    }
}

data class SparkelResponse(val barn: List<SparkelBarn>)
data class SparkelBarn(
    val fornavn: String, val mellomnavn: String?, val etternavn: String,
    val fdato: LocalDate, val status: String, val aktørId: String, val diskresjonskode: String?)