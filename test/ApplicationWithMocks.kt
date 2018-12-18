package no.nav.pleiepenger.api

import io.ktor.server.testing.withApplication
import no.nav.pleiepenger.api.wiremock.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")

class ApplicationWithMocks {

    companion object {
        @JvmStatic fun main(args: Array<String>) {

            val wireMockServer = bootstrap(8081)
            stubSparkelgetId()
            stubSparkelGetBarn()

            val testArgs = arrayOf(
                "-P:ktor.deployment.port=8082",
                "-P:nav.authorization.jwks_uri=" + wireMockServer.getJwksUri(),
                "-P:nav.gateways.sparkel_url=" + wireMockServer.getSparkelUrl()
            )

            withApplication { no.nav.pleiepenger.api.main(testArgs) }
        }
    }
}