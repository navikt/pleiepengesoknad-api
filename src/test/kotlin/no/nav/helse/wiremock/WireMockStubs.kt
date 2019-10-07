package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import io.ktor.http.HttpHeaders
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.WireMockBuilder

internal const val sparkelPath = "/helse-reverse-proxy/sparkel-mock"
internal const val k9OppslagPath = "/k9-oppslag-mock"
private const val aktoerRegisterServerPath = "/helse-reverse-proxy/aktoer-register-mock"
private const val pleiepengesoknadMottakPath = "/helse-reverse-proxy/pleiepengesoknad-mottak-mock"
private const val pleiepengerDokumentPath = "/pleiepenger-dokument-mock"

internal fun WireMockBuilder.pleiepengesoknadApiConfig() = wireMockConfiguration {
    it
      //  .extensions(AktoerRegisterResponseTransformer())
        .extensions(PleiepengerDokumentResponseTransformer())
        .extensions(K9OppslagResponseTransformer())
}

internal fun WireMockServer.stubAktoerRegisterGetAktoerId() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$aktoerRegisterServerPath/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withTransformers("aktoer-register")
            )
    )
    return this
}

internal fun WireMockServer.stubK9OppslagSoker() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("aktør_id"))
            .withQueryParam("a", equalTo("fornavn"))
            .withQueryParam("a", equalTo("mellomnavn"))
            .withQueryParam("a", equalTo("etternavn"))
            .withQueryParam("a", equalTo("fødselsdato"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withTransformers("k9-oppslag-soker")
            )
    )
    return this
}

private fun WireMockServer.stubHealthEndpoint(
    path : String
) : WireMockServer{
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$path")).willReturn(
            WireMock.aResponse()
                .withStatus(200)
        )
    )
    return this
}

private fun WireMockServer.stubHealthEndpointThroughZones(
    path : String
) : WireMockServer{
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$path"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
            WireMock.aResponse()
                .withStatus(200)
        )
    )
    return this
}

internal fun WireMockServer.stubPleiepengerDokumentHealth() = stubHealthEndpoint("$pleiepengerDokumentPath/health")
internal fun WireMockServer.stubPleiepengesoknadMottakHealth() = stubHealthEndpointThroughZones("$pleiepengesoknadMottakPath/health")
internal fun WireMockServer.stubSparkelIsReady() = stubHealthEndpointThroughZones("$sparkelPath/isready")


internal fun WireMockServer.stubLeggSoknadTilProsessering() : WireMockServer{
    WireMock.stubFor(
        WireMock.post(WireMock.urlMatching(".*$pleiepengesoknadMottakPath/v1/soknad"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(202)
            )
    )
    return this
}

internal fun WireMockServer.stubPleiepengerDokument() : WireMockServer{
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$pleiepengerDokumentPath/v1/dokument.*"))
            .willReturn(
                WireMock.aResponse()
                    .withTransformers("PleiepengerDokumentResponseTransformer")
            )
    )
    return this
}


internal fun WireMockServer.getSparkelUrl() = baseUrl() + sparkelPath
internal fun WireMockServer.getK9OppslagUrl() = baseUrl() + k9OppslagPath
internal fun WireMockServer.getAktoerRegisterUrl() = baseUrl() + aktoerRegisterServerPath
internal fun WireMockServer.getPleiepengesoknadMottakUrl() = baseUrl() + pleiepengesoknadMottakPath
internal fun WireMockServer.getPleiepengerDokumentUrl() = baseUrl() + pleiepengerDokumentPath