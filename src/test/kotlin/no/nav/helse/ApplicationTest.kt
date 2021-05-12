package no.nav.helse

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.mellomlagring.started
import no.nav.helse.soknad.Arbeidsforhold
import no.nav.helse.soknad.Arbeidsform
import no.nav.helse.soknad.Land
import no.nav.helse.soknad.Næringstyper
import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.OmsorgstilbudEnkeltDag
import no.nav.helse.soknad.OmsorgstilbudFasteDager
import no.nav.helse.soknad.Regnskapsfører
import no.nav.helse.soknad.SkalJobbe
import no.nav.helse.soknad.VetOmsorgstilbud
import no.nav.helse.soknad.Virksomhet
import no.nav.helse.soknad.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.helse.wiremock.pleiepengesoknadApiConfig
import no.nav.helse.wiremock.stubK9Dokument
import no.nav.helse.wiremock.stubK9DokumentHealth
import no.nav.helse.wiremock.stubK9OppslagArbeidsgivere
import no.nav.helse.wiremock.stubK9OppslagBarn
import no.nav.helse.wiremock.stubK9OppslagSoker
import no.nav.helse.wiremock.stubLeggSoknadTilProsessering
import no.nav.helse.wiremock.stubOppslagHealth
import no.nav.helse.wiremock.stubPleiepengesoknadMottakHealth
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.BeforeClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val fnr = "26104500284"
private const val ikkeMyndigFnr = "12125012345"
private val oneMinuteInMillis = Duration.ofMinutes(1).toMillis()

// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val ikkeMyndigDato = "2050-12-12"

@KtorExperimentalAPI
class ApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .pleiepengesoknadApiConfig()
            .build()
            .stubK9DokumentHealth()
            .stubPleiepengesoknadMottakHealth()
            .stubOppslagHealth()
            .stubLeggSoknadTilProsessering("v1/soknad")
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9OppslagArbeidsgivere()
            .stubK9Dokument()

        val redisServer: RedisServer = RedisServer
            .newRedisServer().started()

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    redisServer = redisServer
                )
            )

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
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "organisasjoner": [{
                    "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                    "organisasjonsnummer": "913548221"
                }, {
                    "navn": "NAV, AVD WALDEMAR THRANES GATE",
                    "organisasjonsnummer": "984054564"
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
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "organisasjoner": []
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
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            leggTilCookie = false
        )
    }

    @Test
    fun `Hente arbeidsgivere med for lav ID level`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Forbidden,
            expectedResponse = null,
            cookie = getAuthCookie(fnr = gyldigFodselsnummerA, level = 3)
        )
    }

    @Test
    fun `Hente arbeidsgivere med ugyldig format på ID-Token`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            cookie = Cookie(listOf("localhost-idtoken=ikkeJwt", "Path=/", "Domain=localhost"))
        )
    }

    @Test
    fun `Hente arbeidsgivere med en utloept cookie`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            cookie = getAuthCookie(gyldigFodselsnummerA, expiry = -(oneMinuteInMillis))
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
    fun `Hente arbeidsgivere hvor fra_og_med er etter til_og_med`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/arbeidsgiver?fra_og_med=2020-01-10&til_og_med=2020-01-01",
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
            path = "/barn",
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
            path = "/barn",
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
            path = "/barn",
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
            path = "/barn",
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
    fun `Hente soeker`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Hente soeker som ikke er myndig`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(
                fødselsnummer = ikkeMyndigFnr,
                fødselsdato = ikkeMyndigDato,
                myndig = false
            ),
            cookie = getAuthCookie(ikkeMyndigFnr)
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
            requestEntity = SøknadUtils.bodyMedFodselsnummerPaaBarn(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                vedleggUrl2 = pdfUrl
            )
        )
    }

    @Test
    fun `Sende soknad uten grad`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedFodselsnummerPaaBarn(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                vedleggUrl2 = pdfUrl
            )
        )
    }

    @Test
    fun `Sende soknad ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
            requestEntity = SøknadUtils.bodyMedFodselsnummerPaaBarn(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                vedleggUrl2 = pdfUrl
            )

        )
    }

    @Test //Denne testen fanger ikke opp om barnets navn blir satt eller ikke. Må undersøke loggen.
    fun `Sende soknad med AktørID som ID på barnet`() {
        val cookie = getAuthCookie("26104500284")
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedAktoerIdPaaBarn(
                aktørId = "10000000001",
                vedleggUrl1 = jpegUrl,
                vedleggUrl2 = pdfUrl
            )
        )
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som har regnskapsfører`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedSelvstendigVirksomheterSomListe(
                vedleggUrl1 = jpegUrl,
                virksomheter = listOf(
                    Virksomhet(
                        næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                        fiskerErPåBladB = false,
                        fraOgMed = LocalDate.now().minusDays(1),
                        tilOgMed = LocalDate.now(),
                        næringsinntekt = 123123,
                        navnPåVirksomheten = "TullOgTøys",
                        registrertINorge = true,
                        organisasjonsnummer = "101010",
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                        regnskapsfører = Regnskapsfører(
                            navn = "Kjell",
                            telefon = "84554"
                        )
                    )
                ),
                selvstendigArbeidsForhold = Arbeidsforhold(
                    skalJobbe = SkalJobbe.NEI,
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
                    skalJobbeTimer = 0.0,
                    skalJobbeProsent = 0.0
                )
            )
        )
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som ikke er gyldig, mangler registrertILand`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "selvstendigVirksomheter[0].registrertIUtlandet",
                      "reason": "Hvis registrertINorge er false må registrertIUtlandet være satt",
                      "invalid_value": null
                    },
                    {
                      "type": "entity",
                      "name": "opptjeningAktivitet.selvstendigNæringsdrivende[0].perioder[2021-02-07/2021-02-08].valideringRegistrertUtlandet",
                      "reason": "[Feil{felt='.landkode', feilkode='påkrevd', feilmelding='landkode må være satt, og kan ikke være null, dersom virksomhet er registrert i utlandet.'}]",
                      "invalid_value": "K9-format feilkode: påkrevd"
                    }
                  ]
                }   
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedSelvstendigVirksomheterSomListe(
                vedleggUrl1 = jpegUrl,
                virksomheter = listOf(
                    Virksomhet(
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
                        )
                    )
                ),
                selvstendigArbeidsForhold = Arbeidsforhold(
                    skalJobbe = SkalJobbe.NEI,
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
                    skalJobbeTimer = 0.0,
                    skalJobbeProsent = 0.0
                )
            )
        )
    }

    @Test
    fun `Sende søknad som inneholder både frilansoppdrag og en selvstendig virksomhet som full json`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity =
            //language=json
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
                  "arbeidsgivere": {
                    "organisasjoner": [
                      
                    ]
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
                    "                $jpegUrl                "
                  ],
                  "harMedsøker": false,
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "frilans": {
                    "startdato": "2019-12-06",
                    "jobberFortsattSomFrilans": false,
                    "arbeidsforhold": {
                      "skalJobbe": "nei",
                      "arbeidsform": "FAST",
                      "jobberNormaltTimer": 40.0,
                      "skalJobbeTimer": 0.0,
                      "skalJobbeProsent": 0.0
                    }
                  },
                  "selvstendigVirksomheter": [
                    {
                      "næringstyper": [
                        "JORDBRUK_SKOGBRUK",
                        "DAGMAMMA",
                        "ANNEN"
                      ],
                      "navnPåVirksomheten": "Tull og tøys",
                      "registrertINorge": true,
                      "organisasjonsnummer": "85577454",
                      "fraOgMed": "2020-02-01",
                      "tilOgMed": "2020-02-13",
                      "næringsinntekt": 9857755,
                            "varigEndring": {
                              "dato": "2020-01-03",
                              "forklaring": "forklaring blablablabla",
                              "inntektEtterEndring": "23423"
                            },
                      "yrkesaktivSisteTreFerdigliknedeÅrene": {
                        "oppstartsdato": "2020-02-01"
                      },
                      "regnskapsfører": {
                        "navn": "Kjell Bjarne",
                        "telefon": "88788"
                      }
                    }
                  ],
                  "selvstendigArbeidsforhold": {
                      "skalJobbe": "nei",
                      "arbeidsform": "FAST",
                      "jobberNormaltTimer": 40.0,
                      "skalJobbeTimer": 0.0,
                      "skalJobbeProsent": 0.0
                  },
                  "tilsynsordning": {
                    "svar": "nei"
                  },
                  "harVærtEllerErVernepliktig" : true
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomet som har flere gyldige virksomheter, men med en feil`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "selvstendigVirksomheter[0].registrertIUtlandet",
                  "reason": "Hvis registrertINorge er false må registrertIUtlandet være satt",
                  "invalid_value": null
                },
                {
                  "type": "entity",
                  "name": "opptjeningAktivitet.selvstendigNæringsdrivende[0].perioder[2021-02-07/2021-02-08].valideringRegistrertUtlandet",
                  "reason": "[Feil{felt='.landkode', feilkode='påkrevd', feilmelding='landkode må være satt, og kan ikke være null, dersom virksomhet er registrert i utlandet.'}]",
                  "invalid_value": "K9-format feilkode: påkrevd"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedSelvstendigVirksomheterSomListe(
                vedleggUrl1 = jpegUrl,
                virksomheter = listOf(
                    Virksomhet(
                        næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                        fiskerErPåBladB = false,
                        fraOgMed = LocalDate.parse("2021-02-07"),
                        tilOgMed = LocalDate.parse("2021-02-08"),
                        næringsinntekt = 1212,
                        navnPåVirksomheten = "TullOgTøys",
                        registrertINorge = false,
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
                    ), Virksomhet(
                        næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                        fiskerErPåBladB = false,
                        fraOgMed = LocalDate.now().minusDays(1),
                        tilOgMed = LocalDate.now(),
                        næringsinntekt = 1212,
                        navnPåVirksomheten = "BariBar",
                        registrertINorge = true,
                        organisasjonsnummer = "10110",
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
                    )
                ),
                selvstendigArbeidsForhold = Arbeidsforhold(
                    skalJobbe = SkalJobbe.NEI,
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
                    skalJobbeTimer = 0.0,
                    skalJobbeProsent = 0.0
                )
            )
        )
    }

    @Test
    fun `Gitt innsendt søknad har tom virksomhets liste med selvstendigArbeidsforhold satt, forvent feil`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "selvstendigArbeidsforhold",
                  "reason": "selvstendigVirksomheter kan ikke være tom dersom selvstendigArbeidsforhold er satt.",
                  "invalid_value": []
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString())
                .copy(
                    selvstendigVirksomheter = listOf(),
                    selvstendigArbeidsforhold = Arbeidsforhold(
                        skalJobbe = SkalJobbe.NEI,
                        arbeidsform = Arbeidsform.FAST,
                        jobberNormaltTimer = 40.0,
                        skalJobbeTimer = 0.0,
                        skalJobbeProsent = 0.0
                    )
                )
                .somJson()
        )
    }

    @Test
    fun `Gitt innsendt søknad har virksomhets liste med selvstendigArbeidsforhold som null, forvent feil`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "selvstendigArbeidsforhold",
                  "reason": "selvstendigArbeidsforhold kan ikke være null dersom selvstendigVirksomheter ikke er tom.",
                  "invalid_value": null
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString())
                .copy(
                    selvstendigVirksomheter = listOf(
                        Virksomhet(
                            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                            fiskerErPåBladB = false,
                            fraOgMed = LocalDate.parse("2021-02-07"),
                            tilOgMed = LocalDate.parse("2021-02-08"),
                            næringsinntekt = 1212,
                            navnPåVirksomheten = "TullOgTøys",
                            registrertINorge = false,
                            registrertIUtlandet = Land("DEU", "Tyskeland"),
                            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now())
                        )
                    ),
                    selvstendigArbeidsforhold = null
                )
                .somJson()
        )
    }

    @Test
    fun `Sende soknad som har skalJobbe lik 'ja', men skalJobbeProsent ulik 100%, skal feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
                  "reason": "skalJobbeProsent er ulik 100%. Dersom skalJobbe = 'ja', så må skalJobbeProsent være 100%",
                  "invalid_value": [
                    {
                      "navn": "Bjeffefirmaet ÆÆÅ",
                      "skalJobbe": "JA",
                      "organisasjonsnummer": "917755736",
                      "jobberNormaltTimer": 0.0,
                      "skalJobbeProsent": 99.0,
                      "vetIkkeEkstrainfo": null,
                      "arbeidsform": "FAST"
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarOrganisasjon(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                skalJobbe = "ja",
                skalJobbeProsent = 99.0
            )
        )
    }

    @Test
    fun `Sende soknad som har skalJobbe lik 'redusert', men skalJobbeProsent ikke ligger mellom 1% - 99,9%, skal feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
                  "reason": "skalJobbeProsent ligger ikke mellom 1% og 99%. Dersom skalJobbe = 'redusert', så må skalJobbeProsent være mellom 1% og 99%",
                  "invalid_value": [
                    {
                      "navn": "Bjeffefirmaet ÆÆÅ",
                      "skalJobbe": "REDUSERT",
                      "organisasjonsnummer": "917755736",
                      "jobberNormaltTimer": 0.0,
                      "skalJobbeProsent": 100.0,
                      "vetIkkeEkstrainfo": null,
                      "arbeidsform": "FAST"
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarOrganisasjon(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                skalJobbe = "redusert",
                skalJobbeProsent = 100.0
            )
        )
    }

    @Test
    fun `Sende soknad som har skalJobbe lik 'nei', men skalJobbeProsent er ulik 0%, skal feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
                  "reason": "skalJobbeProsent er ulik 0%. Dersom skalJobbe = 'nei', så må skalJobbeProsent være 0%",
                  "invalid_value": [
                    {
                      "navn": "Bjeffefirmaet ÆÆÅ",
                      "skalJobbe": "NEI",
                      "organisasjonsnummer": "917755736",
                      "jobberNormaltTimer": 0.0,
                      "skalJobbeProsent": 10.0,
                      "vetIkkeEkstrainfo": null,
                      "arbeidsform": "FAST"
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarOrganisasjon(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                skalJobbe = "nei",
                skalJobbeProsent = 10.0
            )
        )
    }

    @Test
    fun `Sende soknad som har skalJobbe lik 'vet_ikke', men skalJobbeProsent er ulik 0%, skal feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
                  "reason": "skalJobbeProsent er ikke 0%. Dersom skalJobbe = 'vet ikke', så må skalJobbeProsent være 0%",
                  "invalid_value": [
                    {
                      "navn": "Bjeffefirmaet ÆÆÅ",
                      "skalJobbe": "VET_IKKE",
                      "organisasjonsnummer": "917755736",
                      "jobberNormaltTimer": 0.0,
                      "skalJobbeProsent": 10.0,
                      "vetIkkeEkstrainfo": null,
                      "arbeidsform": "FAST"
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarOrganisasjon(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                skalJobbe = "vetIkke",
                skalJobbeProsent = 10.0
            )
        )
    }

    @Test
    fun `Sende soknad som har satt erBarnetInnlagt til true men har ikke oppgitt noen perioder i perioderBarnetErInnlagt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                  "arbeidsgivere": {
                    "organisasjoner": [
                      
                    ]
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
                  "tilsynsordning": {
                    "svar": "nei"
                  },
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
            path = "/soknad",
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
            requestEntity = SøknadUtils.bodyMedFodselsnummerPaaBarn(
                fodselsnummer = gyldigFodselsnummerA,
                vedleggUrl1 = jpegUrl,
                vedleggUrl2 = finnesIkkeUrl
            )
        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {
        val forlangtNavn = SøknadUtils.forLangtNavn()
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "12",
                                "navn": "$forlangtNavn",
                                "skalJobbe": "NEI",
                                "arbeidsform": "FAST"
                            }
                        ]
                    },
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
                  "name": "arbeidsgivere.organisasjoner[0].organisasjonsnummer",
                  "reason": "Ikke gyldig organisasjonsnummer.",
                  "invalid_value": "12"
                },
                {
                  "type": "entity",
                  "name": "arbeidsgivere.organisasjoner[0].navn",
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
                  "name": "java.lang.IllegalArgumentException",
                  "reason": "Til og med dato før fra og med dato: 1990-09-29>1990-09-28",
                  "invalid_value": "K9-format feilkode: nullIllegalArgumentException"
                }
              ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad hvor perioden er over 8 uker(40 virkedager) og man har ikke godkjent det, skal feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "bekrefterPeriodeOver8Uker",
                      "reason": "Hvis perioden er over 8 uker(40 virkedager) må bekrefterPeriodeOver8Uker være true",
                        "invalid_value": null
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarTilOgFraOgBekrefterPeriodeOver8Uker(
                vedleggUrl1 = jpegUrl,
                fraOgMed = "2020-01-01",
                tilOgMed = "2020-02-27",
                bekrefterPeriodeOver8Uker = false
            )
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud, der søker vet alle timer, men både fasteDager og enkeltDager er null`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "omsorgstilbud.fasteDager eller omsorgstilbud.enkeltDager",
                      "reason": "Dersom vetOmsorgstilbud er 'VET_ALLE_TIMER', så må enten 'fasteDager' eller 'enkeltDager' være satt.",
                      "invalid_value": "enkeltDager = null, fasteDager = null"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    omsorgstilbud = Omsorgstilbud(vetOmsorgstilbud = VetOmsorgstilbud.VET_ALLE_TIMER),
                    vedlegg = listOf()
                )
                .somJson()
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud, der søker vet noen timer, men både fasteDager og enkeltDager null`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "omsorgstilbud.fasteDager eller omsorgstilbud.enkeltDager",
                      "reason": "Dersom vetOmsorgstilbud er 'VET_NOEN_TIMER', så må enten 'fasteDager' eller 'enkeltDager' være satt.",
                      "invalid_value": "enkeltDager = null, fasteDager = null"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    omsorgstilbud = Omsorgstilbud(vetOmsorgstilbud = VetOmsorgstilbud.VET_NOEN_TIMER),
                    vedlegg = listOf()
                )
                .somJson()
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud, der søker ikke vet timene, men fasteDager er satt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "omsorgstilbud.fasteDager eller omsorgstilbud.enkeltDager",
                      "reason": "Dersom vetOmsorgstilbud er 'VET_IKKE', så kan verken 'fasteDager' eller 'enkeltDager' være satt.",
                      "invalid_value": "enkeltDager = null, fasteDager = OmsorgstilbudFasteDager(mandag=PT7H, tirsdag=null, onsdag=null, torsdag=null, fredag=null)"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    omsorgstilbud = Omsorgstilbud(
                        vetOmsorgstilbud = VetOmsorgstilbud.VET_IKKE,
                        fasteDager = OmsorgstilbudFasteDager(mandag = Duration.ofHours(7))
                    ),
                    vedlegg = listOf()
                )
                .somJson()
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud, der søker ikke vet timene, men enkeltDager er satt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "omsorgstilbud.fasteDager eller omsorgstilbud.enkeltDager",
                      "reason": "Dersom vetOmsorgstilbud er 'VET_IKKE', så kan verken 'fasteDager' eller 'enkeltDager' være satt.",
                      "invalid_value": "enkeltDager = [Omsorgsdag(dato=2020-01-01, tid=PT7H)], fasteDager = null"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    omsorgstilbud = Omsorgstilbud(
                        vetOmsorgstilbud = VetOmsorgstilbud.VET_IKKE,
                        enkeltDager = listOf(
                            OmsorgstilbudEnkeltDag(dato = LocalDate.parse("2020-01-01"), tid = Duration.ofHours(7))
                        )
                    ),
                    vedlegg = listOf()
                )
                .somJson()
        )
    }

    @Test
    fun `Sende søknad med omsorgstilbud, der både enkeltDager og fasteDager er satt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "omsorgstilbud.fasteDager og omsorgstilbud.enkeltDager",
                      "reason": "Både 'fasteDager' og 'enkeltDager' kan ikke være satt samtidig.",
                      "invalid_value": "enkeltDager = [Omsorgsdag(dato=2020-01-01, tid=PT7H)], fasteDager = OmsorgstilbudFasteDager(mandag=PT7H, tirsdag=null, onsdag=null, torsdag=null, fredag=null)"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils
                .defaultSøknad(UUID.randomUUID().toString()).copy(
                    omsorgstilbud = Omsorgstilbud(
                        vetOmsorgstilbud = VetOmsorgstilbud.VET_ALLE_TIMER,
                        enkeltDager = listOf(
                            OmsorgstilbudEnkeltDag(dato = LocalDate.parse("2020-01-01"), tid = Duration.ofHours(7))
                        ),
                        fasteDager = OmsorgstilbudFasteDager(mandag = Duration.ofHours(7))
                    ),
                    vedlegg = listOf()
                )
                .somJson()
        )
    }

    @Test
    fun `Sende søknad hvor perioden er over 8 uker(40 virkedager) og man har godkjent det, skal ikke feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarTilOgFraOgBekrefterPeriodeOver8Uker(
                vedleggUrl1 = jpegUrl,
                fraOgMed = "2020-01-01",
                tilOgMed = "2020-02-27",
                bekrefterPeriodeOver8Uker = true
            )
        )
    }

    @Test
    fun `Sende søknad hvor perioden er 8 uker(40 virkedager), skal ikke feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarTilOgFraOgBekrefterPeriodeOver8Uker(
                vedleggUrl1 = jpegUrl,
                fraOgMed = "2020-01-01",
                tilOgMed = "2020-02-26"
            )
        )
    }

    @Test
    fun `Sende søknad hvor perioden er under 8 uker(40 virkedager), skal ikke feile`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.bodyMedJusterbarTilOgFraOgBekrefterPeriodeOver8Uker(
                vedleggUrl1 = jpegUrl,
                fraOgMed = "2020-01-01",
                tilOgMed = "2020-02-25"
            )
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
}
