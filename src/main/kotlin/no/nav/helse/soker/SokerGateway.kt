package no.nav.helse.soker

import io.ktor.client.HttpClient
import io.prometheus.client.Histogram
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.buildURL
import no.nav.helse.general.monitoredHttpRequest
import no.nav.helse.general.prepareHttpRequestBuilder
import no.nav.helse.systembruker.SystemBrukerTokenService
import java.net.URL

private val sokerOppslagHistogram = Histogram.build(
    "histogram_oppslag_soker",
    "Tidsbruk for oppslag på persondata om søker"
).register()

class SokerGateway(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
    private val aktoerService: AktoerService,
    private val systemBrukerTokenService: SystemBrukerTokenService,
    private val apiGatewayApiKey: ApiGatewayApiKey
) {
    suspend fun getSoker(fnr: Fodselsnummer,
                         callId : CallId) : Soker {
        val url = buildURL(
            baseUrl = baseUrl,
            pathParts = listOf(
                "api",
                "person",
                aktoerService.getAktorId(fnr, callId).value
            )
        )

        val httpRequest = prepareHttpRequestBuilder(
            authorization = systemBrukerTokenService.getAuthorizationHeader(),
            url = url,
            callId = callId,
            apiGatewayApiKey = apiGatewayApiKey
        )

        val response = monitoredHttpRequest<SparkelResponse> (
            httpClient = httpClient,
            httpRequest = httpRequest,
            histogram = sokerOppslagHistogram
        )

        return Soker(
            fornavn = response.fornavn,
            mellomnavn = response.mellomnavn,
            etternavn = response.etternavn,
            kjonn = response.kjønn.toLowerCase(),
            fodselsnummer = fnr.value
        )
    }
}

data class SparkelResponse(val fornavn: String, val mellomnavn: String?, val etternavn: String, val kjønn : String)