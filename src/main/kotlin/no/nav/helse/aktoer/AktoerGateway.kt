package no.nav.helse.aktoer

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.prometheus.client.Histogram
import no.nav.helse.general.*
import no.nav.helse.general.HttpRequest
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.soker.SparkelResponse
import no.nav.helse.systembruker.SystemBrukerTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

/**
 * https://app-q1.adeo.no/aktoerregister/swagger-ui.html
 */

private const val AKTOERREGISTER_CORRELATION_ID_HEADER = "Nav-Call-Id"

private val logger: Logger = LoggerFactory.getLogger("nav.AktoerGateway")

private val getAktoerIdHistogram = Histogram.build(
    "histogram_hente_aktoer_id_fra_fnr",
    "Tidsbruk for henting av Aktør ID fra Fødselsnummer"
).register()

class AktoerGateway(
    private val httpClient: HttpClient,
    baseUrl: URL,
    private val systemBrukerTokenService: SystemBrukerTokenService,
    private val apiGatewayApiKey: ApiGatewayApiKey
) {
    private val completeUrl : URL = HttpRequest.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api","v1","identer"),
        queryParameters = mapOf(
            Pair("gjeldende", "true"),
            Pair("identgruppe", "AktoerId")
        )
    )

    suspend fun getAktoerId(
        fnr: Fodselsnummer,
        callId: CallId
    ) : AktoerId {

        val httpRequest = HttpRequestBuilder()
        httpRequest.header(HttpHeaders.Authorization, systemBrukerTokenService.getAuthorizationHeader())
        httpRequest.header(AKTOERREGISTER_CORRELATION_ID_HEADER, callId.value)
        httpRequest.header("Nav-Consumer-Id", "pleiepengesoknad-api")
        httpRequest.header("Nav-Personidenter", fnr.value)
        httpRequest.header(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
        httpRequest.method = HttpMethod.Get
        httpRequest.accept(ContentType.Application.Json)
        httpRequest.url(completeUrl)

        val httpResponse = HttpRequest.monitored<Map<String, IdentResponse>>(
            httpClient = httpClient,
            httpRequest = httpRequest,
            histogram = getAktoerIdHistogram
        )

        if (!httpResponse.containsKey(fnr.value)) {
            throw IllegalStateException("Svar fra '$completeUrl' inneholdt ikke data om det forsespurte fødselsnummeret.")
        }

        val identResponse =  httpResponse.get(key = fnr.value)

        if (identResponse!!.feilmelding!= null) {
            logger.warn("Mottok feilmelding fra AktørRegister : '${identResponse.feilmelding}'")
        }

        if (identResponse.identer.size != 1) {
            throw IllegalStateException("Fikk ${identResponse.identer.size} AktørID'er for det forsespurte fødselsnummeret mot '$completeUrl'")
        }

        val aktoerId = AktoerId(identResponse.identer[0].ident)
        logger.info("Resolved AktørID $aktoerId")
        return aktoerId
    }
}

data class Ident(val ident: String, val identgruppe: String)
data class IdentResponse(val feilmelding : String?, val identer : List<Ident>)