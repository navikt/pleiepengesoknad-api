package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.Extension
import no.nav.helse.ApplicationWithMocks
import no.nav.security.oidc.test.support.JwkGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.bootstrap")
private const val jwkSetPath = "/auth-mock/jwk-set"
private const val sparkelPath = "/sparkel-mock"


fun bootstrapWiremock(port: Int? = null,
              extensions : Array<Extension> = arrayOf()) : WireMockServer {
    val wireMockConfiguration = WireMockConfiguration.options()
        .extensions(AuthMockJwtResponseTransformer())
        .extensions(AuthMockCookieResponseTransformer())

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

    authMockJwt()
    authMockCookie()
    authMockJwkSet()

    stubSparkelgetId()
    stubSparkelGetBarn()
    stubSparkelGetAnsettelsesforhold()

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

fun WireMockServer.getJwksUri() : String {
    return baseUrl() + jwkSetPath
}

fun WireMockServer.getSparkelUrl() : String {
    return baseUrl() + sparkelPath
}