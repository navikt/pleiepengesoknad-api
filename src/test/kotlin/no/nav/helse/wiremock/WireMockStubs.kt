package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder

internal const val k9OppslagPath = "/k9-selvbetjening-oppslag-mock"
private const val pleiepengesoknadMottakPath = "/pleiepengesoknad-mottak-mock"
private const val k9MellomlagringPath = "/k9-mellomlagring-mock"

internal fun WireMockBuilder.pleiepengesoknadApiConfig() = wireMockConfiguration {
    it
        .extensions(SokerResponseTransformer())
        .extensions(BarnResponseTransformer())
        .extensions(ArbeidsgivereResponseTransformer())
        .extensions(K9MellomlagringResponseTransformer())
}


internal fun WireMockServer.stubK9OppslagSoker() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
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

internal fun WireMockServer.stubK9OppslagBarn(simulerFeil: Boolean = false) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("barn[].aktør_id"))
            .withQueryParam("a", equalTo("barn[].fornavn"))
            .withQueryParam("a", equalTo("barn[].mellomnavn"))
            .withQueryParam("a", equalTo("barn[].etternavn"))
            .withQueryParam("a", equalTo("barn[].fødselsdato"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withTransformers("k9-oppslag-barn")
            )
    )
    return this
}

internal fun WireMockServer.stubK9OppslagArbeidsgivere(simulerFeil: Boolean = false) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("arbeidsgivere[].organisasjoner[].organisasjonsnummer"))
            .withQueryParam("a", equalTo("arbeidsgivere[].organisasjoner[].navn"))
            .withQueryParam("fom", AnythingPattern()) // vurder regex som validerer dato-format
            .withQueryParam("tom", AnythingPattern()) // vurder regex som validerer dato-format
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withTransformers("k9-oppslag-arbeidsgivere")
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

internal fun WireMockServer.stubK9MellomlagringHealth() = stubHealthEndpoint("$k9MellomlagringPath/health")
internal fun WireMockServer.stubPleiepengesoknadMottakHealth() = stubHealthEndpoint("$pleiepengesoknadMottakPath/health")
internal fun WireMockServer.stubOppslagHealth() = stubHealthEndpoint("$k9OppslagPath/health")

internal fun WireMockServer.stubLeggSoknadTilProsessering(path: String) : WireMockServer{
    WireMock.stubFor(
        WireMock.post(WireMock.urlMatching(".*$pleiepengesoknadMottakPath/$path"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(202)
            )
    )
    return this
}

internal fun WireMockServer.stubK9Mellomlagring() : WireMockServer{
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$k9MellomlagringPath/v1/dokument.*"))
            .willReturn(
                WireMock.aResponse()
                    .withTransformers("K9MellomlagringResponseTransformer")
            )
    )
    return this
}

internal fun WireMockServer.getK9OppslagUrl() = baseUrl() + k9OppslagPath
internal fun WireMockServer.getPleiepengesoknadMottakUrl() = baseUrl() + pleiepengesoknadMottakPath
internal fun WireMockServer.getK9MellomlagringUrl() = baseUrl() + k9MellomlagringPath
