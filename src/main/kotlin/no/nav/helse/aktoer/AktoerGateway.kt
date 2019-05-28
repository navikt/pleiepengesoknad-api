package no.nav.helse.aktoer

import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.SystemCredentialsProvider
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration

/**
 * https://app-q1.adeo.no/aktoerregister/swagger-ui.html
 */

private const val AKTOERREGISTER_CORRELATION_ID_HEADER = "Nav-Call-Id"

private val logger: Logger = LoggerFactory.getLogger("nav.AktoerGateway")

class AktoerGateway(
    private val monitoredHttpClient : MonitoredHttpClient,
    baseUrl: URL,
    private val systemCredentialsProvider: SystemCredentialsProvider
) {
    private val aktoerIdUrl : URL = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api","v1","identer"),
        queryParameters = mapOf(
            Pair("gjeldende", listOf("true")),
            Pair("identgruppe", listOf("AktoerId"))
        )
    )

    private val fodselsnummerUrl : URL = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api","v1","identer"),
        queryParameters = mapOf(
            Pair("gjeldende", listOf("true")),
            Pair("identgruppe", listOf("NorskIdent"))
        )
    )

    private suspend fun get(
        url: URL,
        personIdent: String,
        callId: CallId
    ) : String {

        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemCredentialsProvider.getAuthorizationHeader())
        httpRequest.header(HttpHeaders.XCorrelationId, callId.value) // For proxy
        httpRequest.header(AKTOERREGISTER_CORRELATION_ID_HEADER, callId.value)
        httpRequest.header("Nav-Consumer-Id", "pleiepengesoknad-api")
        httpRequest.header("Nav-Personidenter", personIdent)
        httpRequest.method = HttpMethod.Get
        httpRequest.accept(ContentType.Application.Json)
        httpRequest.url(url)

        val httpResponse = request(httpRequest)

        if (!httpResponse.containsKey(personIdent)) {
            throw IllegalStateException("Svar fra '$url' inneholdt ikke data om det forsespurte ident.")
        }

        val identResponse =  httpResponse.get(key = personIdent)

        if (identResponse!!.feilmelding!= null) {
            logger.warn("Mottok feilmelding fra AktørRegister : '${identResponse.feilmelding}'")
        }

        if (identResponse.identer.size != 1) {
            throw IllegalStateException("Fikk ${identResponse.identer.size} Identer for den forsespurte ID'en mot '$url'")
        }

        return identResponse.identer[0].ident
    }

    suspend fun hentAktoerId(
        fnr: Fodselsnummer,
        callId: CallId
    ) : AktoerId {
        return AktoerId(get(
            url = aktoerIdUrl,
            personIdent = fnr.value,
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

    private suspend fun request(
        httpRequest: HttpRequestBuilder
    ) : Map<String, AktoerRegisterIdentResponse> {
        return Retry.retry(
            operation = "hente-identer",
            tries = 3,
            initialDelay = Duration.ofMillis(100),
            maxDelay = Duration.ofMillis(300),
            logger = logger
        ) {
            monitoredHttpClient.requestAndReceive<Map<String, AktoerRegisterIdentResponse>>(
                httpRequestBuilder = HttpRequestBuilder().takeFrom(httpRequest)
            )
        }
    }

}

data class AktoerRegisgerIdent(val ident: String, val identgruppe: String)
data class AktoerRegisterIdentResponse(val feilmelding : String?, val identer : List<AktoerRegisgerIdent>)