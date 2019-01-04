package no.nav.helse

import io.ktor.server.testing.withApplication
import no.nav.helse.kafka.bootstrapKafka
import no.nav.helse.kafka.getPassword
import no.nav.helse.kafka.getUsername
import no.nav.helse.wiremock.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")

class ApplicationWithMocks {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockSerer = bootstrapWiremock(8081)
            val kafkaEnvironment = bootstrapKafka()

            val testArgs = arrayOf(
                "-P:ktor.deployment.port=8082",
                "-P:nav.authorization.jwks_uri=${wireMockSerer.getJwksUri()}",
                "-P:nav.gateways.sparkel_url=${wireMockSerer.getSparkelUrl()}",
                "-P:nav.kafka.bootstrap_servers=${kafkaEnvironment.brokersURL}",
                "-P:nav.kafka.username=${kafkaEnvironment.getUsername()}",
                "-P:nav.kafka.password=${kafkaEnvironment.getPassword()}"
            )

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockSerer.stop()
                    kafkaEnvironment.tearDown()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.helse.main(testArgs) }
        }
    }
}