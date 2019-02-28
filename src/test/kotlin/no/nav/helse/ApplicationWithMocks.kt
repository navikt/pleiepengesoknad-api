package no.nav.helse

import io.ktor.server.testing.withApplication
import no.nav.helse.wiremock.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")

class ApplicationWithMocks {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockSerer = bootstrapWiremock(8081)

            val testArgs = TestConfiguration.asArray(TestConfiguration.asMap(wireMockServer = wireMockSerer))

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockSerer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.helse.main(testArgs) }
        }
    }
}