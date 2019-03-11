package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.Extension
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import no.nav.helse.ApplicationWithMocks
import no.nav.security.oidc.test.support.JwkGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.bootstrap")
private const val jwkSetPath = "/auth-mock/jwk-set"
private const val sparkelPath = "/sparkel-mock"
private const val authorizationServerPath = "/authorization-server-mock/token"
private const val aktoerRegisterServerPath = "/aktoer-register-mock"
private const val pleiepengesoknadProsesseringPath = "/pleiepengesoknad-prosessering-mock"
private const val pleiepengerDokumentPath = "/pleiepenger-dokument-mock"



fun bootstrapWiremock(port: Int? = null,
              extensions : Array<Extension> = arrayOf()) : WireMockServer {
    val wireMockConfiguration = WireMockConfiguration.options()
        .extensions(AuthMockJwtResponseTransformer())
        .extensions(AuthMockCookieResponseTransformer())
        .extensions(AktoerRegisterMockGetAktoerIdResponseTransformer())
        .extensions(PleiepengerDokumentResponseTransformer())

    extensions.forEach {
        wireMockConfiguration.extensions(it)
    }

    if (port == null) {
        wireMockConfiguration.dynamicPort()
    } else {
        wireMockConfiguration.port(port)
    }

    val wireMockServer = WireMockServer(wireMockConfiguration)

    wireMockServer.start()
    WireMock.configureFor(wireMockServer.port())

    // Auth
    authMockJwt()
    authMockCookie()
    authMockJwkSet()
    stubStsGetAccessToken()

    // Sparkel
    stubReadiness(basePath = sparkelPath)
    stubSparkelGetSoker()
    stubSparkelGetArbeidsgivere()

    // Aktørregister
    aktoerRegisterGetAktoerId()
    stubReadiness(basePath = aktoerRegisterServerPath, readinessPath = "internal/isAlive")

    // Pleiepengsoknad-prosessering
    stubLeggSoknadTilProsessering()
    stubReadiness(basePath = pleiepengesoknadProsesseringPath)

    // Pleiepenger-dokument
    stubPleiepengerDokument()

    logger.info("Mock available on '{}'", wireMockServer.baseUrl())
    return wireMockServer
}

private fun authMockJwt() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/auth-mock/jwt")).willReturn(
            WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("auth-mock-jwt-response-transformer")
        )
    )
}
private fun authMockCookie() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/auth-mock/cookie.*")).willReturn(
            WireMock.aResponse()
                .withTransformers("auth-mock-cookie-response-transformer")
        )
    )
}

private fun authMockJwkSet() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(jwkSetPath)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(ApplicationWithMocks::class.java.getResource(JwkGenerator.DEFAULT_JWKSET_FILE).readText())
        )
    )
}

private fun aktoerRegisterGetAktoerId() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$aktoerRegisterServerPath/.*")).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withTransformers("aktoer-register-mock-get-aktoer-id")
        )
    )
}

private fun stubReadiness(basePath: String,
                          readinessPath : String = "isready") {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*$basePath/$readinessPath"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
            )
    )
}

private fun stubLeggSoknadTilProsessering() {
    WireMock.stubFor(
        WireMock.post(WireMock.urlMatching(".*$pleiepengesoknadProsesseringPath/v1/soknad"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(202)
            )
    )
}

private fun stubPleiepengerDokument() {
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$pleiepengerDokumentPath.*")).willReturn(
            WireMock.aResponse()
                .withTransformers("PleiepengerDokumentResponseTransformer")
        )
    )
}

fun WireMockServer.getJwksUri() : String {
    return baseUrl() + jwkSetPath
}

fun WireMockServer.getSparkelUrl() : String {
    return baseUrl() + sparkelPath
}

fun WireMockServer.getAuthorizationTokenUrl() : String {
    return baseUrl() + authorizationServerPath
}

fun WireMockServer.getAktoerRegisterUrl() : String {
    return baseUrl() + aktoerRegisterServerPath
}

fun WireMockServer.getPleiepengesoknadProsesseringUrl() : String {
    return baseUrl() + pleiepengesoknadProsesseringPath
}

fun WireMockServer.getPleiepengerDokumentUrl() : String {
    return baseUrl() + pleiepengerDokumentPath
}