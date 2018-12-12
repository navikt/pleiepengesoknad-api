package no.nav.pleiepenger.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.io.*
import io.ktor.config.HoconApplicationConfig
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.id.IdResponse
import org.junit.BeforeClass

class ApplicationTest {

    private val objectMapper = configureObjectMapper()

    private companion object {
        val engine = TestApplicationEngine(createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
            module {
                pleiepengesoknadapi(HttpClient(MockEngine {
                    if (url.encodedPath == "/foo") {
                        MockHttpResponse(
                            call,
                            HttpStatusCode.OK,
                            ByteReadChannel("1234".toByteArray(Charsets.UTF_8)),
                            headersOf()
                        )
                    } else {
                        responseError(HttpStatusCode.NotFound, "Not Found ${url.encodedPath}")
                    }
                }) {
                    expectSuccess = false
                })
            }
        })

        @BeforeClass @JvmStatic fun setup(){
            engine.start(wait = false)
        }
    }


    @Test
    fun getIdTest() {

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/id/12345678911") {
                addHeader("accept", "application/json")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(IdResponse("1234"), objectMapper.readValue(response.content!!))
            }
        }
    }


    @Test
    fun testRoo2t() {

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/soker/1234") {
                addHeader("accept", "application/json")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}