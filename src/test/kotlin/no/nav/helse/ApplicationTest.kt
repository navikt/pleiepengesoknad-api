package no.nav.helse

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.TestUtils.Companion.getAuthCookie
import no.nav.helse.arbeidsgiver.orgQueryName
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.innsyn.InnsynBarn
import no.nav.helse.k9format.defaultK9FormatPSB
import no.nav.helse.k9format.defaultK9SakInnsynSøknad
import no.nav.helse.mellomlagring.started
import no.nav.helse.soknad.ArbeidIPeriode
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.Enkeltdag
import no.nav.helse.soknad.Ferieuttak
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.HistoriskOmsorgstilbud
import no.nav.helse.soknad.JobberIPeriodeSvar
import no.nav.helse.soknad.Næringstyper
import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.PlanlagtOmsorgstilbud
import no.nav.helse.soknad.Regnskapsfører
import no.nav.helse.soknad.SelvstendigNæringsdrivende
import no.nav.helse.soknad.Virksomhet
import no.nav.helse.soknad.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.helse.wiremock.pleiepengesoknadApiConfig
import no.nav.helse.wiremock.stubK9Mellomlagring
import no.nav.helse.wiremock.stubK9MellomlagringHealth
import no.nav.helse.wiremock.stubK9OppslagArbeidsgivere
import no.nav.helse.wiremock.stubK9OppslagArbeidsgivereMedPrivate
import no.nav.helse.wiremock.stubK9OppslagBarn
import no.nav.helse.wiremock.stubK9OppslagSoker
import no.nav.helse.wiremock.stubOppslagHealth
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationTest::class.java)

        // Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
        private val gyldigFodselsnummerA = "02119970078"
        private val fnr = "26104500284"
        private val ikkeMyndigFnr = "12125012345"
        private val oneMinuteInMillis = Duration.ofMinutes(1).toMillis()

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withLoginServiceSupport()
            .pleiepengesoknadApiConfig()
            .build()
            .stubK9MellomlagringHealth()
            .stubOppslagHealth()
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9OppslagArbeidsgivere()
            .stubK9OppslagArbeidsgivereMedOrgNummer()
            .stubK9OppslagArbeidsgivereMedPrivate()
            .stubK9Mellomlagring()
            .stubSifInnsynApi(
                k9SakInnsynSøknader = listOf(
                    defaultK9SakInnsynSøknad(
                        barn = InnsynBarn(
                            fødselsdato = LocalDate.parse("2000-08-27"),
                            fornavn = "BARNESEN",
                            mellomnavn = "EN",
                            etternavn = "BARNESEN",
                            aktør_id = "1000000000001",
                            identitetsnummer = "02119970078"
                        ),
                        søknad = defaultK9FormatPSB()
                    )
                )
            )

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaKonsumer = kafkaEnvironment.testConsumer()

        val redisServer: RedisServer = RedisServer
            .newRedisServer().started()

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment,
                    redisServer = redisServer
                )
            )

            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })


        @BeforeAll
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            redisServer.stop()
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
    fun `Hente arbeidsgivere`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.OK,
            //language=json
            expectedResponse = """
            {
                "organisasjoner": [
                  {
                    "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                    "organisasjonsnummer": "913548221",
                    "ansattFom": "2011-09-03",
                    "ansattTom": "2012-06-30"
                  }, 
                  {
                    "navn": "NAV, AVD WALDEMAR THRANES GATE",
                    "organisasjonsnummer": "984054564",
                    "ansattFom": "2011-09-03",
                    "ansattTom": null
                  }
                ],
                "privateArbeidsgivere": null

            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `Hente arbeidsgivere med organisasjonsnummer`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ORGANISASJONER_URL?$orgQueryName=977302390&$orgQueryName=984054564",
            expectedCode = HttpStatusCode.OK,
            expectedResponse =
            //language=json
            """
            {
                "organisasjoner": [
                  {
                    "navn": "INMETA CONSULTING AS",
                    "organisasjonsnummer": "977302390",
                    "ansattFom": null,
                    "ansattTom": null
                  },
                  {
                    "navn": "NAV, AVD WALDEMAR THRANES GATE",
                    "organisasjonsnummer": "984054564",
                    "ansattFom": null,
                    "ansattTom": null
                  }
                ],
                "privateArbeidsgivere": null

            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `Hente arbeidsgivere inkludert private`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30&private_arbeidsgivere=true",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
              "organisasjoner": [
                {
                  "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                  "organisasjonsnummer": "913548221",
                  "ansattFom": null,
                  "ansattTom": null
                },
                {
                  "navn": "NAV, AVD WALDEMAR THRANES GATE",
                  "organisasjonsnummer": "984054564",
                  "ansattFom": null,
                  "ansattTom": null
                }
              ],
              "privateArbeidsgivere": [
                {
                    "offentligIdent": "10047206508",
                    "ansattFom": "2014-07-01",
                    "ansattTom": "2015-12-31"
                }
              ]
            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `Finner ingen arbeidsgivere når organisasjonsnummer ikke er funnet`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ORGANISASJONER_URL?$orgQueryName=925568600",
            expectedCode = HttpStatusCode.OK,
            expectedResponse =
            //language=json
            """
            {
                "organisasjoner": [],
                "privateArbeidsgivere": null

            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `Finner arbeidsgiver uten navn`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ORGANISASJONER_URL?$orgQueryName=995784637",
            expectedCode = HttpStatusCode.OK,
            expectedResponse =
            //language=json
            """
            {
                "organisasjoner": [
                  {
                    "navn": null,
                    "organisasjonsnummer": "995784637",
                    "ansattFom": null,
                    "ansattTom": null
                  }
                ],
                "privateArbeidsgivere": null
            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `gitt orgnummer er ugyldig, forvent valideringsfeil`() {

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ORGANISASJONER_URL?$orgQueryName=977302390&$orgQueryName=ugyldig_orgnummer",
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse =
            //language=json
            """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "status": 400,
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "instance": "about:blank",
                    "invalid_parameters": [{
                        "type": "query",
                        "name": "$orgQueryName[1]",
                        "reason": "Query parameter $orgQueryName[1] er av ugyldig format",
                        "invalid_value": "ugyldig_orgnummer"
                    }]
                }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `gitt orgnummer mangler på query parameter, forvent valideringsfeil`() {

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ORGANISASJONER_URL",
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse =
            //language=json
            """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "status": 400,
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "instance": "about:blank",
                    "invalid_parameters": [{
                        "type": "query",
                        "name": "$orgQueryName",
                        "reason": "Påkrevd query parameter '$orgQueryName' er ikke satt.",
                        "invalid_value": null
                    }]
                }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
    }

    @Test
    fun `Feil ved henting av arbeidsgivere skal returnere en tom liste`() {
        wireMockServer.stubK9OppslagArbeidsgivere(simulerFeil = true)
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "organisasjoner": [],
                "privateArbeidsgivere": []
            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
        wireMockServer.stubK9OppslagArbeidsgivere()
    }

    @Test
    fun `Hente arbeidsgivere uten cookie satt`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30&private_arbeidsgivere=true",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            leggTilCookie = false
        )
    }

    @Test
    fun `Hente arbeidsgivere med for lav ID level`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30&private_arbeidsgivere=true",
            expectedCode = HttpStatusCode.Forbidden,
            expectedResponse = null,
            cookie = getAuthCookie(fnr = gyldigFodselsnummerA, level = 3)
        )
    }

    @Test
    fun `Hente arbeidsgivere med ugyldig format på ID-Token`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30&private_arbeidsgivere=true",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            cookie = Cookie(listOf("localhost-idtoken=ikkeJwt", "Path=/", "Domain=localhost"))
        )
    }

    @Test
    fun `Hente arbeidsgivere med en utloept cookie`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2019-01-01&til_og_med=2019-01-30&private_arbeidsgivere=true",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            cookie = getAuthCookie(gyldigFodselsnummerA, expiry = -(oneMinuteInMillis))
        )
    }

    @Test
    fun `Hente arbeidsgivere med ugyldig format paa til og fra`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=heisann&til_og_med=hadet",
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
            path = "$ARBEIDSGIVER_URL",
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
    fun `Hente arbeidsgivere hvor fra_og_med er etter til_og_med`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "$ARBEIDSGIVER_URL?fra_og_med=2020-01-10&til_og_med=2020-01-01",
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "query",
                  "name": "fra_og_med",
                  "reason": "Fra og med må være før eller lik til og med.",
                  "invalid_value": "2020-01-10"
                },
                {
                  "type": "query",
                  "name": "til_og_med",
                  "reason": "Til og med må være etter eller lik fra og med.",
                  "invalid_value": "2020-01-01"
                }
              ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Hente barn og sjekk eksplisit at identitetsnummer ikke blir med ved get kall`() {

        val respons = requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            //language=json
            expectedResponse = """
            {
              "barn": [
                {
                  "fødselsdato": "2000-08-27",
                  "fornavn": "BARN",
                  "mellomnavn": "EN",
                  "etternavn": "BARNESEN",
                  "aktørId": "1000000000001",
                  "harSammeAdresse": true
                },
                {
                  "fødselsdato": "2001-04-10",
                  "fornavn": "BARN",
                  "mellomnavn": "TO",
                  "etternavn": "BARNESEN",
                  "aktørId": "1000000000002",
                  "harSammeAdresse": true
                }
              ]
            }
            """.trimIndent()
        )

        val responsSomJSONArray = JSONObject(respons).getJSONArray("barn")

        assertFalse(responsSomJSONArray.getJSONObject(0).has("identitetsnummer"))
        assertFalse(responsSomJSONArray.getJSONObject(1).has("identitetsnummer"))
    }

    @Test
    fun `Henting av barn`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barn": [{
                    "fødselsdato": "2000-08-27",
                    "fornavn": "BARN",
                    "mellomnavn": "EN",
                    "etternavn": "BARNESEN",
                    "aktørId": "1000000000001",
                    "harSammeAdresse": true
                }, {
                    "fødselsdato": "2001-04-10",
                    "fornavn": "BARN",
                    "mellomnavn": "TO",
                    "etternavn": "BARNESEN",
                    "aktørId": "1000000000002",
                    "harSammeAdresse": true
                }]
            }
            """.trimIndent(),
            cookie = getAuthCookie(fnr)
        )
    }

    @Test
    fun `Har ingen registrerte barn`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barn": []
            }
            """.trimIndent(),
            cookie = getAuthCookie("07077712345")
        )
    }

    @Test
    fun `Feil ved henting av barn skal returnere tom liste`() {
        wireMockServer.stubK9OppslagBarn(simulerFeil = true)
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barn": []
            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerA)
        )
        wireMockServer.stubK9OppslagBarn()
    }

    fun expectedGetSokerJson(
        fødselsnummer: String,
        fødselsdato: String = "1997-05-25",
        myndig: Boolean = true
    ) = """
    {
        "etternavn": "MORSEN",
        "fornavn": "MOR",
        "mellomnavn": "HEISANN",
        "fødselsnummer": "$fødselsnummer",
        "aktørId": "12345",
        "fødselsdato": "$fødselsdato",
        "myndig": $myndig
    }
    """.trimIndent()

    @Test
    fun `Hente søker`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Hente søker som ikke er myndig`() {
        wireMockServer.stubK9OppslagSoker(
            statusCode = HttpStatusCode.fromValue(451),
            responseBody =
            //language=json
            """
            {
                "detail": "Policy decision: DENY - Reason: (NAV-bruker er i live AND NAV-bruker er ikke myndig)",
                "instance": "/meg",
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451
            }
            """.trimIndent()
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.fromValue(451),
            expectedResponse =
            //language=json
            """
            {
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451,
                "instance": "/soker",
                "detail": "Tilgang nektet."
            }
            """.trimIndent(),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )

        wireMockServer.stubK9OppslagSoker() // reset til default mapping
    }

    @Test
    fun `Sende søknad`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.now().minusDays(3),
            tilOgMed = LocalDate.now().plusDays(4),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true,
                ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(2),
                    )
                )
            ),
            vedlegg = listOf(URL(jpegUrl))
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = søknad.somJson()
        )

        hentOgAssertSøknad(JSONObject(søknad.somJson()))
    }

    @Test
    fun `Validerer vedlegg hvor et ikke finnes`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val vedlegg1 = engine.jpegUrl(cookie)
        val vedlegg2 = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = VALIDER_VEDLEGG_URL,
            expectedResponse = """
                {
                  "vedleggUrl": [
                    "http://localhost:80/vedlegg/finnes-ikke"
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.OK,
            cookie = cookie,
            requestEntity = """
                {
                  "vedleggUrl": [
                    "http://localhost:80/vedlegg/finnes-ikke",
                    "$vedlegg1",
                    "$vedlegg2"
                  ]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Validerer vedlegg hvor alle finnes`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val vedlegg1 = engine.jpegUrl(cookie)
        val vedlegg2 = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = VALIDER_VEDLEGG_URL,
            expectedResponse = """
                {
                  "vedleggUrl": []
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.OK,
            cookie = cookie,
            requestEntity = """
                {
                  "vedleggUrl": [    
                    "$vedlegg1",
                    "$vedlegg2"
                  ]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende soknad ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = """
                {
                    "type": "/problem-details/unauthorized",
                    "title": "unauthorized",
                    "status": 403,
                    "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
                    "instance": "about:blank"
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.Forbidden,
            cookie = cookie,
            requestEntity = SøknadUtils.defaultSøknad().copy(
                vedlegg = listOf(URL(jpegUrl)),
            ).somJson()

        )
    }

    @Test
    fun `Sende søknad med AktørID som ID på barnet`() {
        val cookie = getAuthCookie("26104500284")
        val jpegUrl = engine.jpegUrl(cookie)
        val søknad = SøknadUtils.defaultSøknad().copy(
            fraOgMed = LocalDate.now().minusDays(3),
            tilOgMed = LocalDate.now().plusDays(4),
            selvstendigNæringsdrivende = null,
            omsorgstilbud = null,
            vedlegg = listOf(URL(jpegUrl)),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true,
                ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(2),
                    )
                )
            ),
            barn = BarnDetaljer(
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen",
                aktørId = "1000000000001",
                fødselsnummer = null
            )
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = søknad.somJson()
        )

        hentOgAssertSøknad(JSONObject(søknad))
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som har regnskapsfører`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val søknad = SøknadUtils.defaultSøknad().copy(
            omsorgstilbud = null,
            ferieuttakIPerioden = null,
            fraOgMed = LocalDate.now().minusDays(3),
            tilOgMed = LocalDate.now().plusDays(4),
            vedlegg = listOf(URL(jpegUrl)),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                Virksomhet(
                    næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                    fiskerErPåBladB = false,
                    fraOgMed = LocalDate.now().minusDays(1),
                    tilOgMed = LocalDate.now(),
                    næringsinntekt = 123123,
                    navnPåVirksomheten = "TullOgTøys",
                    registrertINorge = true,
                    organisasjonsnummer = "926032925",
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                    regnskapsfører = Regnskapsfører(
                        navn = "Kjell",
                        telefon = "84554"
                    ),
                    harFlereAktiveVirksomheter = true
                ),
                arbeidsforhold = Arbeidsforhold(
                    jobberNormaltTimer = 37.5,
                    historiskArbeid = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.JA,
                        erLiktHverUke = false,
                        enkeltdager = listOf(
                            Enkeltdag(
                                dato = LocalDate.parse("2021-01-01"),
                                tid = Duration.ofHours(7).plusMinutes(30)
                            )
                        ),
                        fasteDager = null
                    ),
                    planlagtArbeid = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.JA,
                        erLiktHverUke = false,
                        enkeltdager = listOf(
                            Enkeltdag(
                                dato = LocalDate.parse("2021-01-02"),
                                tid = Duration.ofHours(7).plusMinutes(30)
                            )
                        ),
                        fasteDager = null
                    )
                )
            )
        )
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = søknad.somJson()
        )
        hentOgAssertSøknad(JSONObject(søknad))
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som ikke er gyldig, mangler registrertILand`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            //language=json
            expectedResponse = """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "selvstendingNæringsdrivende.virksomhet.registrertIUtlandet",
                  "reason": "Hvis registrertINorge er false må registrertIUtlandet være satt",
                  "invalid_value": null
                },
                {
                  "type": "entity",
                  "name": "ytelse.opptjeningAktivitet.selvstendigNæringsdrivende[0].organisasjonsnummer.valid",
                  "reason": "Organisasjonsnummer må være gyldig.",
                  "invalid_value": "K9-format feilkode: ugyldigOrgNummer"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.defaultSøknad().copy(
                fraOgMed = LocalDate.now().minusDays(3),
                tilOgMed = LocalDate.now().plusDays(3),
                ferieuttakIPerioden = null,
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    virksomhet = Virksomhet(
                        næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                        fiskerErPåBladB = false,
                        fraOgMed = LocalDate.parse("2021-02-07"),
                        tilOgMed = LocalDate.parse("2021-02-08"),
                        næringsinntekt = 1233123,
                        navnPåVirksomheten = "TullOgTøys",
                        registrertINorge = false,
                        organisasjonsnummer = "101010",
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                        regnskapsfører = Regnskapsfører(
                            navn = "Kjell",
                            telefon = "84554"
                        ),
                        harFlereAktiveVirksomheter = true
                    ),
                    arbeidsforhold = Arbeidsforhold(
                        jobberNormaltTimer = 40.0,
                        historiskArbeid = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.JA,
                            erLiktHverUke = false,
                            enkeltdager = listOf(
                                Enkeltdag(
                                    dato = LocalDate.parse("2021-01-01"),
                                    tid = Duration.ofHours(7).plusMinutes(30)
                                )
                            ),
                            fasteDager = null
                        ),
                        planlagtArbeid = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.NEI,
                            erLiktHverUke = null,
                            enkeltdager = null,
                            fasteDager = null,
                            jobberProsent = 50.0
                        ),
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad som har satt erBarnetInnlagt til true men har ikke oppgitt noen perioder i perioderBarnetErInnlagt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "Utenlandsopphold[1]",
                      "reason": "Hvis erBarnetInnlagt er true så må perioderBarnetErInnlagt inneholde minst en periode",
                      "invalid_value": "perioderBarnetErInnlagt"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity =
            //language=JSON
            """
                {
                  "new_version": true,
                  "sprak": "nb",
                  "barn": {
                    "navn": null,
                    "fødselsnummer": "03028104560",
                    "aktørId": null,
                    "fodselsdato": null
                  },
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": false,
                    "skalBoIUtlandetNeste12Mnd": false,
                    "utenlandsoppholdSiste12Mnd": [
                      
                    ],
                    "utenlandsoppholdNeste12Mnd": [
                      
                    ]
                  },
                  "fraOgMed": "2020-02-01",
                  "tilOgMed": "2020-02-13",
                  "vedlegg": [
                    "$jpegUrl"
                  ],
                  "harMedsøker": false,
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "utenlandsoppholdIPerioden" : 
                    {
                      "skalOppholdeSegIUtlandetIPerioden": true,
                      "opphold": [
                        {
                          "fraOgMed": "2019-10-10",
                          "tilOgMed": "2019-11-10",
                          "landkode": "SE",
                          "landnavn": "Sverige"
                        },
                        {
                          "landnavn": "USA",
                          "landkode": "US",
                          "fraOgMed": "2020-01-08",
                          "tilOgMed": "2020-01-09",
                          "erUtenforEos": true,
                          "erBarnetInnlagt": true,
                          "perioderBarnetErInnlagt": [],
                          "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                        }
                      ]
                    },
                    "harVærtEllerErVernepliktig" : true
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende soknad hvor et av vedleggene peker på et ikke eksisterende vedlegg`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val finnesIkkeUrl = jpegUrl.substringBeforeLast("/").plus("/").plus(UUID.randomUUID().toString())

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = """
            {
                "type": "/problem-details/invalid-request-parameters",
                "title": "invalid-request-parameters",
                "status": 400,
                "detail": "Requesten inneholder ugyldige paramtere.",
                "instance": "about:blank",
                "invalid_parameters": [{
                    "type": "entity",
                    "name": "vedlegg",
                    "reason": "Mottok referanse til 2 vedlegg, men fant kun 1 vedlegg.",
                    "invalid_value": ["$jpegUrl", "$finnesIkkeUrl"]
                }]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.defaultSøknad().copy(
                fraOgMed = LocalDate.now().minusDays(3),
                tilOgMed = LocalDate.now().plusDays(4),
                ferieuttakIPerioden = FerieuttakIPerioden(
                    skalTaUtFerieIPerioden = true,
                    ferieuttak = listOf(
                        Ferieuttak(
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusDays(2),
                        )
                    )
                ),
                vedlegg = listOf(URL(jpegUrl), URL(finnesIkkeUrl)),
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {
        val forlangtNavn = SøknadUtils.forLangtNavn()
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity =
            //language=JSON
            """
                {
                    "barn": {
                        "navn": "",
                        "fødselsnummer": "2909912345"
                    },
                    "fraOgMed": "1990-09-29",
                    "tilOgMed": "1990-09-28",
                    "arbeidsgivere" : [
                      {
                        "navn" : "$forlangtNavn",
                        "organisasjonsnummer" : 12345,
                        "arbeidsforhold" : {
                            "jobberNormaltTimer": 37.5,
                            "historisk": null,
                            "planlagt": null
                        }
                      }  
                    ],
                    "vedlegg": [
                        "http://localhost:8080/ikke-vedlegg/123",
                        null
                    ],
                    "medlemskap" : {},
                    "utenlandsoppholdIPerioden": {
                        "skalOppholdeSegIUtlandetIPerioden": false,
                        "opphold": []
                    },
                    "harForstattRettigheterOgPlikter": false,
                    "ferieuttakIPerioden": {
                        "skalTaUtFerieIPeriode": true,
                        "ferieuttak": [
                          {
                            "fraOgMed": "2020-01-05",
                            "tilOgMed": "2020-01-07"
                          }
                        ]
                    },
                    "harVærtEllerErVernepliktig" : true
                }
                """.trimIndent(),
            //language=json
            expectedResponse = """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "barn.fødselsnummer",
                  "reason": "Ikke gyldig fødselsnummer.",
                  "invalid_value": "2909912345"
                },
                {
                  "type": "entity",
                  "name": "arbeidsgivere.arbeidsforholdAnsatt[0].organisasjonsnummer",
                  "reason": "Ikke gyldig organisasjonsnummer.",
                  "invalid_value": "12345"
                },
                {
                  "type": "entity",
                  "name": "arbeidsgivere.arbeidsforholdAnsatt[0].navn",
                  "reason": "Navnet på organisasjonen kan ikke være tomt, og kan maks være 100 tegn.",
                  "invalid_value": "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"
                },
                {
                  "type": "entity",
                  "name": "fraOgMed",
                  "reason": "Fra og med må være før eller lik til og med.",
                  "invalid_value": "1990-09-29"
                },
                {
                  "type": "entity",
                  "name": "tilOgMed",
                  "reason": "Til og med må være etter eller lik fra og med.",
                  "invalid_value": "1990-09-28"
                },
                {
                  "type": "entity",
                  "name": "vedlegg[0]",
                  "reason": "Ikke gyldig vedlegg URL.",
                  "invalid_value": "http://localhost:8080/ikke-vedlegg/123"
                },
                {
                  "type": "entity",
                  "name": "vedlegg[1]",
                  "reason": "Ikke gyldig vedlegg URL.",
                  "invalid_value": null
                },
                {
                  "type": "entity",
                  "name": "medlemskap.harBoddIUtlandetSiste12Mnd",
                  "reason": "medlemskap.harBoddIUtlandetSiste12Mnd kan ikke være null",
                  "invalid_value": null
                },
                {
                  "type": "entity",
                  "name": "medlemskap.skalBoIUtlandetNeste12Mnd",
                  "reason": "medlemskap.skalBoIUtlandetNeste12Mnd kan ikke være null",
                  "invalid_value": null
                },
                {
                  "type": "entity",
                  "name": "harMedsøker",
                  "reason": "harMedsøker kan ikke være null",
                  "invalid_value": null
                },
                {
                  "type": "entity",
                  "name": "harBekreftetOpplysninger",
                  "reason": "Opplysningene må bekreftes for å sende inn søknad.",
                  "invalid_value": false
                },
                {
                  "type": "entity",
                  "name": "harForstattRettigheterOgPlikter",
                  "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                  "invalid_value": false
                },
                    {
                  "type": "entity",
                  "name": "ytelse.søknadsperiode.perioder[0]",
                  "reason": "Fra og med (FOM) må være før eller lik til og med (TOM).",
                  "invalid_value": "K9-format feilkode: ugyldigPeriode"
                },
                {
                  "type": "entity",
                  "name": "ytelse.arbeidstid.arbeidstakerList[0].organisasjonsnummer.valid",
                  "reason": "Organisasjonsnummer må være gyldig.",
                  "invalid_value": "K9-format feilkode: ugyldigOrgNummer"
                }
              ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud planlagt er satt men både ukedager og enkeltdager er null`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "omsorgstilbud.planlagt.ukedager eller omsorgstilbud.planlagt.enkeltdager",
                      "reason": "Dersom omsorgstilbud.planlagt er satt så må enten 'ukedager' eller 'enkeltdager' være satt.",
                      "invalid_value": "enkeltdager = null, ukedager = null"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    frilans = null,
                    selvstendigNæringsdrivende = null,
                    arbeidsgivere = null,
                    omsorgstilbud = Omsorgstilbud(
                        planlagt = PlanlagtOmsorgstilbud()
                    ),
                    vedlegg = listOf()
                )
                .somJson()
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud, der historiske omsorgstilbud inneholder datoer lik eller etter dagens dato`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "omsorgstilbud.historisk.enkeltdager",
                      "reason": "Historiske enkeltdager inneholder datoer som er enten lik eller senere enn dagens dato.",
                      "invalid_value": "enkeltdager = [Enkeltdag(dato=${LocalDate.now()}, tid=PT7H)]"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    fraOgMed = LocalDate.now(),
                    tilOgMed = LocalDate.now().plusDays(1),
                    omsorgstilbud = Omsorgstilbud(
                        historisk = HistoriskOmsorgstilbud(
                            enkeltdager = listOf(
                                Enkeltdag(dato = LocalDate.now(), tid = Duration.ofHours(7))
                            ),
                        )
                    ),
                    vedlegg = listOf(),
                    ferieuttakIPerioden = null,
                    frilans = null,
                    selvstendigNæringsdrivende = null,
                    arbeidsgivere = null,
                )
                .somJson()
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
    fun `Test opplasting av for stort vedlegg`() {
        engine.handleRequestUploadImage(
            cookie = getAuthCookie(gyldigFodselsnummerA),
            vedlegg = ByteArray(8 * 1024 * 1024 + 10),
            contentType = "image/png",
            fileName = "big_picture.png",
            expectedCode = HttpStatusCode.PayloadTooLarge
        )
    }

    @Test
    fun `endringsmelding - endringer innefor gyldighetsperiode`() {
        val cookie = getAuthCookie(fnr)
        val søknadId = UUID.randomUUID().toString()
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        //language=json
        val endringsmelding = """
            {
             "søknadId": "$søknadId",
              "id": "123",
              "språk": "nb",
              "mottattDato": "$mottattDato",
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ytelse": {
                "type": "PLEIEPENGER_SYKT_BARN",
                "barn": {
                  "norskIdentitetsnummer": "02119970078"
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "organisasjonsnummer": "917755736",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-01": {
                            "jobberNormaltTimerPerDag": "PT1H0M",
                            "faktiskArbeidTimerPerDag": "PT0H"
                          }
                        }
                      }
                    }
                  ]
                },
                "tilsynsordning": {
                  "perioder": {
                    "2021-01-01/2021-01-01": {
                      "etablertTilsynTimerPerDag": "PT2H0M"
                    }
                  }
                }
              }
            }
        """.trimIndent()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = ENDRINGSMELDING_URL,
            cookie = cookie,
            expectedCode = HttpStatusCode.Accepted,
            expectedResponse = null,
            requestEntity = endringsmelding
        )

        hentOgAsserEndringsmelding(
            //language=json
            """
           {
             "søker": {
               "mellomnavn": "HEISANN",
               "etternavn": "MORSEN",
               "aktørId": "12345",
               "fødselsdato": "1997-05-25",
               "fornavn": "MOR",
               "fødselsnummer": "26104500284",
               "myndig": true
             },
             "harBekreftetOpplysninger": true,
             "harForståttRettigheterOgPlikter": true,
             "k9Format": {
               "søknadId": "$søknadId",
               "versjon": "1.0.0",
               "mottattDato": "$mottattDato",
               "språk": "nb",
               "søker": {
                 "norskIdentitetsnummer": "26104500284"
               },
               "ytelse": {
                 "type": "PLEIEPENGER_SYKT_BARN",
                 "søknadsperiode": [],
                 "endringsperiode": [],
                 "trekkKravPerioder": [],
                 "barn": {
                   "norskIdentitetsnummer": "02119970078",
                   "fødselsdato": null
                 },
                 "tilsynsordning": {
                   "perioder": {
                     "2021-01-01/2021-01-01": {
                       "etablertTilsynTimerPerDag": "PT2H"
                     }
                   }
                 },
                 "arbeidstid": {
                   "frilanserArbeidstidInfo": null,
                   "arbeidstakerList": [
                     {
                       "organisasjonsnummer": "917755736",
                       "norskIdentitetsnummer": null,
                       "arbeidstidInfo": {
                         "perioder": {
                           "2021-01-01/2021-01-01": {
                             "faktiskArbeidTimerPerDag": "PT0S",
                             "jobberNormaltTimerPerDag": "PT1H"
                           }
                         }
                       }
                     }
                   ],
                   "selvstendigNæringsdrivendeArbeidstidInfo": null
                 },
                 "bosteder": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "lovbestemtFerie": {
                   "perioder": {}
                 },
                 "omsorg": {
                   "beskrivelseAvOmsorgsrollen": null,
                   "relasjonTilBarnet": null
                 },
                 "utenlandsopphold": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "nattevåk": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "infoFraPunsj": null,
                 "dataBruktTilUtledning": null,
                 "beredskap": {
                   "perioder": {},
                   "perioderSomSkalSlettes": {}
                 },
                 "uttak": {
                   "perioder": {}
                 },
                 "opptjeningAktivitet": {}
               },
               "journalposter": [],
               "begrunnelseForInnsending": {
                 "tekst": null
               }
             }
           }
            """.trimIndent(),
            JSONObject(endringsmelding)
        )
    }

    @Test
    fun `endringsmelding - endringer utenfor gyldighetsperiode`() {
        val cookie = getAuthCookie(fnr)
        val søknadId = UUID.randomUUID().toString()
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        //language=json
        val endringsmelding = """
                {
                  "søknadId": "$søknadId",
                  "språk": "nb",
                  "mottattDato": "$mottattDato",
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "ytelse": {
                    "type": "PLEIEPENGER_SYKT_BARN",
                    "barn": {
                      "norskIdentitetsnummer": "02119970078"
                    },
                    "arbeidstid": {
                      "arbeidstakerList": [
                        {
                          "organisasjonsnummer": "917755736",
                          "arbeidstidInfo": {
                            "perioder": {
                              "2021-01-07/2021-01-07": {
                                "jobberNormaltTimerPerDag": "PT1H0M",
                                "faktiskArbeidTimerPerDag": "PT0H"
                              }
                            }
                          }
                        }
                      ]
                    }
                  }
                }
            """.trimIndent()
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = ENDRINGSMELDING_URL,
            cookie = cookie,
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse =
            //language=json
            """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "ytelse.arbeidstid.arbeidstakerList[0].perioder",
                      "reason": "Perioden er utenfor gyldig interval. Gyldig interva: ([[2018-01-01, 2021-01-01]]), Ugyldig periode: 2021-01-07/2021-01-07",
                      "invalid_value": "K9-format feilkode: ugyldigPeriode"
                    }
                  ]
                }
            """.trimIndent(),
            requestEntity = endringsmelding
        )
    }

    @Test
    fun `gitt to mellomlagrede verdier på samme person, fovent at begge mellomlagres, og de de ikke overskriver hverandre`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        val mellomlagringSøknad = """
                {
                    "mellomlagring": "soknad"
                }
            """.trimIndent()


        val mellomlagringEndringsmelding = """
                {
                    "mellomlagring": "endringsmelding"
                }
            """.trimIndent()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = MELLOMLAGRING_URL,
            cookie = cookie,
            expectedCode = HttpStatusCode.NoContent,
            expectedResponse = null,
            requestEntity = mellomlagringSøknad
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = ENDRINGSMELDING_MELLOMLAGRING_URL,
            cookie = cookie,
            expectedCode = HttpStatusCode.NoContent,
            expectedResponse = null,
            requestEntity = mellomlagringEndringsmelding
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = MELLOMLAGRING_URL,
            cookie = cookie,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = mellomlagringSøknad
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = ENDRINGSMELDING_MELLOMLAGRING_URL,
            cookie = cookie,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = mellomlagringEndringsmelding
        )
    }

    private fun hentOgAsserEndringsmelding(forventenEndringsmelding: String, endringsmelding: JSONObject) {
        val komplettEndringsmelding = kafkaKonsumer.hentEndringsmelding(endringsmelding.getString("søknadId"))

        JSONAssert.assertEquals(
            forventenEndringsmelding,
            komplettEndringsmelding.data,
            JSONCompareMode.STRICT
        )
    }

    private fun requestAndAssert(
        httpMethod: HttpMethod,
        path: String,
        requestEntity: String? = null,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        leggTilCookie: Boolean = true,
        cookie: Cookie = getAuthCookie(fnr)
    ): String? {
        val respons: String?
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
                respons = response.content
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
        return respons
    }

    private fun hentOgAssertSøknad(søknad: JSONObject){
        val hentet = kafkaKonsumer.hentSøknad(søknad.getString("søknadId"))
        assertGyldigSøknad(søknad, hentet.data)
    }

    private fun assertGyldigSøknad(
        søknadSendtInn: JSONObject,
        søknadFraTopic: JSONObject
    ) {
        assertTrue(søknadFraTopic.has("søker"))
        assertTrue(søknadFraTopic.has("mottatt"))
        assertTrue(søknadFraTopic.has("k9FormatSøknad"))

        val k9Format = søknadFraTopic.getJSONObject("k9FormatSøknad")
        assertEquals("PLEIEPENGER_SYKT_BARN", k9Format.getJSONObject("ytelse").getString("type"))

        assertEquals(søknadSendtInn.getString("søknadId"), søknadFraTopic.getString("søknadId"))

        if(søknadSendtInn.has("vedleggUrls") && !søknadSendtInn.getJSONArray("vedleggUrls").isEmpty){
            assertEquals(søknadSendtInn.getJSONArray("vedleggUrls").length(),søknadFraTopic.getJSONArray("vedleggUrls").length())
        }
    }
}
