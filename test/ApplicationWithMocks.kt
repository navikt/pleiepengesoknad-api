package no.nav.pleiepenger.api

import io.ktor.server.testing.withApplication
import no.nav.pleiepenger.api.wiremock.bootstrap
import no.nav.pleiepenger.api.wiremock.getJwksUri
import no.nav.pleiepenger.api.wiremock.stubSparkelgetId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")

class ApplicationWithMocks {

    companion object {
        @JvmStatic fun main(args: Array<String>) {

            val wireMockServer = bootstrap(8081)
            stubSparkelgetId()

            val testArgs = arrayOf(
                "-P:ktor.deployment.port=8082",
                "-P:nav.authorization.jwks_uri=" + wireMockServer.getJwksUri(),
                "-P:nav.gateways.sparkel_url=" + wireMockServer.baseUrl() + "/sparkel-mock"
            )

            withApplication { no.nav.pleiepenger.api.main(testArgs) }
        }
    }
}