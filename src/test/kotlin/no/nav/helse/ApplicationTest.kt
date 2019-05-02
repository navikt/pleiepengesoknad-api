package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

private const val fnr = "290990123456"
private val oneMinuteInMillis = Duration.ofMinutes(1).toMillis()
private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationTest")
// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"

@KtorExperimentalAPI
class ApplicationTest {

    private companion object {

        val wireMockServer: WireMockServer = bootstrapWiremock()

        fun getConfig() : ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(TestConfiguration.asMap(wireMockServer = wireMockServer))
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })


        @BeforeClass
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Hente arbedisgivere uten cookie satt`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            leggTilCookie = false
        )
    }

    @Test
    fun `Hente arbreidsgivere med for lav ID level`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Forbidden,
            expectedResponse = null,
            cookie = getAuthCookie(fnr, authLevel = "Level3")
        )
    }

    @Test
    fun `Hente arbeidsgivere med en utloept cookie`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            cookie = getAuthCookie(fnr, expiry = -(oneMinuteInMillis))
        )
    }

    @Test
    fun `Hente arbeidsgivere med ugyldig format paa til og fra`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=heisann&til_og_med=hadet",
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "status": 400,
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "instance": "about:blank",
                    "invalid_parameters": [{
                        "type": "query",
                        "name": "fra_og_med",
                        "reason": "Må settes og være på gyldig format (YYYY-MM-DD)",
                        "invalid_value": "heisann"
                    }, {
                        "type": "query",
                        "name": "til_og_med",
                        "reason": "Må settes og være på og gyldig format (YYYY-MM-DD)",
                        "invalid_value": "hadet"
                    }]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Hente arbeidsgivere uten til og fra satt`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver",
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "status": 400,
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "instance": "about:blank",
                    "invalid_parameters": [{
                        "type": "query",
                        "name": "fra_og_med",
                        "reason": "Må settes og være på gyldig format (YYYY-MM-DD)",
                        "invalid_value": null
                    }, {
                        "type": "query",
                        "name": "til_og_med",
                        "reason": "Må settes og være på og gyldig format (YYYY-MM-DD)",
                        "invalid_value": null
                    }]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Henting av barn gir en tom liste`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/barn",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
                {
                    "barn" : []
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Hente soeker`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Sende soknad`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = Soknad.body(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                vedleggUrl2 = pdfUrl
            )

        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {
        val forlangtNavn = Soknad.forLangtNavn()
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = """
                {
                    "barn": {
                        "navn": "",
                        "fodselsnummer": "29099012345"
                    },
                    "relasjon_til_barnet": "mor",
                    "fra_og_med": "1990-09-29",
                    "til_og_med": "1990-09-28",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "12",
                                "navn": "$forlangtNavn"
                            }
                        ]
                    },
                    "grad": 120,
                    "vedlegg": [],
                    "medlemskap" : {}
                }
                """.trimIndent(),
            expectedResponse = """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "status": 400,
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "instance": "about:blank",
                    "invalid_parameters": [{
                        "type": "entity",
                        "name": "barn.fodselsnummer",
                        "reason": "Ikke gyldig fødselsnummer.",
                        "invalid_value": "29099012345"
                    }, {
                        "type": "entity",
                        "name": "barn.navn",
                        "reason": "Navn på barnet kan ikke være tomt, og kan maks være 100 tegn.",
                        "invalid_value": ""
                    }, {
                        "type": "entity",
                        "name": "arbeidsgivere.organisasjoner[0].organisasjonsnummer",
                        "reason": "Ikke gyldig organisasjonsnummer.",
                        "invalid_value": "12"
                    }, {
                        "type": "entity",
                        "name": "arbeidsgivere.organisasjoner[0].navn",
                        "reason": "Navnet på organisasjonen kan ikke være tomt, og kan maks være 100 tegn.",
                        "invalid_value": "$forlangtNavn"
                    }, {
                        "type": "entity",
                        "name": "fra_og_med",
                        "reason": "Fra og med må være før til og med.",
                        "invalid_value": "1990-09-29"
                    }, {
                        "type": "entity",
                        "name": "til_og_med",
                        "reason": "Til og med må være etter fra og med.",
                        "invalid_value": "1990-09-28"
                    }, {
                        "type": "entity",
                        "name": "vedlegg",
                        "reason": "Det må sendes minst et vedlegg.",
                        "invalid_value": []
                    }, {
                        "type": "entity",
                        "name": "grad",
                        "reason": "Grad må være mellom 20 og 100.",
                        "invalid_value": 120
                    },{
                        "type": "entity",
                        "name": "medlemskap.har_bodd_i_utlandet_siste_12_mnd",
                        "reason": "Må settes til true eller false.",
                        "invalid_value": null
	                },{
                        "type": "entity",
                        "name": "medlemskap.skal_bo_i_utlandet_neste_12_mnd",
                        "reason": "Må settes til true eller false.",
                        "invalid_value": null
	                },{
                        "type": "entity",
                        "name": "har_medsoker",
                        "reason": "Må settes til true eller false.",
                        "invalid_value": null
	                }]
                }
            """.trimIndent()
        )
    }


    @Test
    fun `Test haandtering av vedlegg`() {
        val cookie = getAuthCookie(fnr)
        val jpeg = "vedlegg/iPhone_6.jpg".fromResources().readBytes()

        with(engine) {
            // LASTER OPP VEDLEGG
            val url = handleRequestUploadImage(
                cookie = cookie,
                vedlegg = jpeg
            )
            val path = Url(url).fullPath
            // HENTER OPPLASTET VEDLEGG
            handleRequest(HttpMethod.Get, path) {
                addHeader("Cookie", cookie.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(Arrays.equals(jpeg, response.byteContent))
                // SLETTER OPPLASTET VEDLEGG
                handleRequest(HttpMethod.Delete, path) {
                    addHeader("Cookie", cookie.toString())
                }.apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                    // VERIFISERER AT VEDLEGG ER SLETTET
                    handleRequest(HttpMethod.Get, path) {
                        addHeader("Cookie", cookie.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                    }
                }
            }
        }
    }

    @Test
    fun `Test opplasting av ikke stottet vedleggformat`() {
        engine.handleRequestUploadImage(
            cookie = getAuthCookie(gyldigFodselsnummerA),
            vedlegg = "jwkset.json".fromResources().readBytes(),
            contentType = "application/json",
            fileName = "jwkset.json",
            expectedCode = HttpStatusCode.BadRequest
        )
    }

    @Test
    fun `Test opplasting av for stort vedlegg` () {
        engine.handleRequestUploadImage(
            cookie = getAuthCookie(gyldigFodselsnummerA),
            vedlegg = ByteArray(8 * 1024 * 1024 + 10),
            contentType = "image/png",
            fileName = "big_picture.png",
            expectedCode = HttpStatusCode.PayloadTooLarge
        )
    }

    private fun requestAndAssert(httpMethod: HttpMethod,
                                 path : String,
                                 requestEntity : String? = null,
                                 expectedResponse : String?,
                                 expectedCode : HttpStatusCode,
                                 leggTilCookie : Boolean = true,
                                 cookie : Cookie = getAuthCookie(fnr)) {
        with(engine) {
            handleRequest(httpMethod, path) {
                if (leggTilCookie) addHeader(HttpHeaders.Cookie, cookie.toString())
                logger.info("Request Entity = $requestEntity")
                addHeader(HttpHeaders.Accept, "application/json")
                if (requestEntity != null) addHeader(HttpHeaders.ContentType, "application/json")
                if (requestEntity != null) setBody(requestEntity)
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
    }
}