package no.nav.pleiepenger.api

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.server.testing.withApplication
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.security.oidc.test.support.JwkGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")


class ApplicationWithMocks {

    companion object {
        @JvmStatic fun main(args: Array<String>) {

            val wireMockServer = WireMockServer(WireMockConfiguration.options()
                //.dynamicPort()
                .port(8084)
                .extensions(AuthMockJwtResponseTransformer())
                .extensions(AuthMockCookieResponseTransformer())
            )

            wireMockServer.start()
            configureFor(wireMockServer.port())

            stubGetId()
            authMockJwt()
            authMockCookie()
            authMockJwkSet()

            logger.info("Mock available on '{}'", wireMockServer.baseUrl())


            val testArgs = arrayOf(
                "-P:ktor.deployment.port=8085",
                "-P:nav.gateways.idGateway.baseUrl=" + wireMockServer.baseUrl() + "/id-gateway"
            )

            withApplication { no.nav.pleiepenger.api.main(testArgs) }
        }

        fun stubGetId()  {
            stubFor(get(urlPathMatching("/id-gateway/.*")).willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":\"1234\"}")
            ))
        }

        fun authMockJwt() {
            stubFor(
                get(urlPathMatching("/auth-mock/jwt")).willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("auth-mock-jwt-response-transformer")
                )
            )
        }
        fun authMockCookie() {
            stubFor(
                get(urlPathMatching("/auth-mock/cookie.*")).willReturn(
                    aResponse()
                        .withTransformers("auth-mock-cookie-response-transformer")
                )
            )
        }

        fun authMockJwkSet() {
            stubFor(
                get(urlPathMatching("/auth-mock/jwk-set")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(ApplicationWithMocks::class.java.getResource(JwkGenerator.DEFAULT_JWKSET_FILE).readText())
                )
            )
        }
    }
}