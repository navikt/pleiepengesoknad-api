package no.nav.helse.aktoer

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.*

import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.general.*
import no.nav.helse.general.systemauth.AuthorizationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration

/**
 * https://app-q1.adeo.no/aktoerregister/swagger-ui.html
 */

class AktoerGateway(
    baseUrl: URI,
    private val authorizationService: AuthorizationService
) {

    private companion object {
        private const val HENTE_AKTOER_ID_OPERATION = "hente-aktoer-id"
        private val logger: Logger = LoggerFactory.getLogger(AktoerGateway::class.java)
        private val objectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    private val aktoerIdUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api","v1","identer"),
        queryParameters = mapOf(
            Pair("gjeldende", listOf("true")),
            Pair("identgruppe", listOf("AktoerId"))
        )
    ).toString()

    private val fodselsnummerUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api","v1","identer"),
        queryParameters = mapOf(
            Pair("gjeldende", listOf("true")),
            Pair("identgruppe", listOf("NorskIdent"))
        )
    ).toString()

    private suspend fun get(
        url: String,
        personIdent: String,
        callId: CallId
    ) : String {
        val authorizationHeader = authorizationService.getAuthorizationHeader()

        val httpRequest = url
            .httpGet()
            .header(
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.Accept to "application/json",
                "Nav-Consumer-Id" to "pleiepengesoknad-prosessering",
                "Nav-Personidenter" to personIdent,
                "Nav-Call-Id" to callId.value
            )

        val httpResponse = Retry.retry(
            operation = HENTE_AKTOER_ID_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request,_, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_AKTOER_ID_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }
            result.fold(
                { success -> objectMapper.readValue<Map<String,AktoerRegisterIdentResponse>>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av Aktør ID.")
                }
            )
        }


        if (!httpResponse.containsKey(personIdent)) {
            throw IllegalStateException("Svar fra '$url' inneholdt ikke data om det forsespurte fødselsnummeret.")
        }

        val identResponse =  httpResponse.get(key = personIdent)

        if (identResponse!!.feilmelding!= null) {
            logger.warn("Mottok feilmelding fra AktørRegister : '${identResponse.feilmelding}'")
        }

        if (identResponse.identer == null) {
            throw IllegalStateException("Fikk 0 AktørID'er for det forsespurte fødselsnummeret mot '$url'")
        }

        if (identResponse.identer.size != 1) {
            throw IllegalStateException("Fikk ${identResponse.identer.size} AktørID'er for det forsespurte fødselsnummeret mot '$url'")
        }

        val aktoerId = AktoerId(identResponse.identer[0].ident)
        logger.trace("Resolved AktørID $aktoerId")
        return aktoerId.value
    }

    suspend fun hentAktoerId(
        norskIdent: NorskIdent,
        callId: CallId
    ) : AktoerId {
        return AktoerId(get(
            url = aktoerIdUrl,
            personIdent = norskIdent.getValue(),
            callId = callId
        ))
    }

    suspend fun hentNorskIdent(
        aktoerId: AktoerId,
        callId: CallId
    ) : NorskIdent {
        return get(
            url = fodselsnummerUrl,
            personIdent = aktoerId.value,
            callId = callId
        ).tilNorskIdent()
    }
}

data class AktoerRegisterIdent(val ident: String, val identgruppe: String)
data class AktoerRegisterIdentResponse(val feilmelding : String?, val identer : List<AktoerRegisterIdent>?)