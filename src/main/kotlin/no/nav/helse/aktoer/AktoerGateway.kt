package no.nav.helse.aktoer

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.prometheus.client.Histogram
import no.nav.helse.general.*
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.systembruker.SystemBrukerTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

/**
 * https://app-q1.adeo.no/aktoerregister/swagger-ui.html
 */

private val logger: Logger = LoggerFactory.getLogger("nav.AktoerGateway")

private val getAktoerIdHistogram = Histogram.build(
    "histogram_hente_aktoer_id_fra_fnr",
    "Tidsbruk for henting av Aktør ID fra Fødselsnummer"
).register()

private val getAktoerCounter = monitoredOperationtCounter(
    name = "counter_hente_aktoer_id_fra_fnr",
    help = "Antall Aktør ID'er hentet fra Fødselsnummer"
)

class AktoerGateway(
    val httpClient: HttpClient,
    baseUrl: URL,
    val systemBrukerTokenService: SystemBrukerTokenService
) {
    private val completeUrl : URL = buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("api","v1","identer"),
        queryParameters = mapOf(
            Pair("gjeldende", "true"),
            Pair("identgruppe", "AktoerId")
        )
    )

    suspend fun getAktoerId(fnr: Fodselsnummer) : AktoerId {
        val httpRequest = prepareHttpRequestBuilder(
            authorization = systemBrukerTokenService.getAuthorizationHeader(),
            url = completeUrl
        )

        httpRequest.header("Nav-Call-Id", "to-do-add") // TODO
        httpRequest.header("Nav-Consumer-Id", "pleiepengesoknad-api")
        httpRequest.header("Nav-Personidenter", fnr.value)

        val httpResponse = monitoredOperation(
            operation = { httpClient.get<Map<String, IdentResponse>>(httpRequest) },
            counter = getAktoerCounter,
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