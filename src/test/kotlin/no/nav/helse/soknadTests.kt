package no.nav.helse

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.github.tomakehurst.wiremock.http.Cookie
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.helse.general.validation.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val logger: Logger = LoggerFactory.getLogger("nav.soknadValidationTest")

fun gyldigSoknad(
    engine: TestApplicationEngine,
    cookie: Cookie
) {
    with(engine) {
        val url = handleRequestUploadImage(
            cookie = cookie,
            vedlegg = "vedlegg/nav-logo.png".fromResources(),
            fileName = "nav-logo.png",
            contentType = "image/png"
        )
        handleRequest(HttpMethod.Post, "/soknad") {
            addHeader("Accept", "application/json")
            addHeader("Cookie", cookie.toString())
            setBody(body(vedleggUrl = url))
        }.apply {
            assertEquals(HttpStatusCode.Accepted, response.status())
        }
    }
}


fun obligatoriskeFelterIkkeSatt(
    engine: TestApplicationEngine,
    cookie: Cookie) {
    assertFailsWith(MissingKotlinParameterException::class) {
        with(engine) {
            with(handleRequest(HttpMethod.Post, "/soknad") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
                setBody("{}")
            }) {}
        }
    }
}

fun ugyldigInformasjonOmBarn(
    engine: TestApplicationEngine,
    cookie: Cookie) {

    expectViolationException(
        engine = engine,
        cookie = cookie,
        body = body(fodselsnummer = "123", vedleggUrl = "http://localhost:8080/vedlegg/123123"),
        numberOfExpectedViolations = 1
    )

}

private fun expectViolationException(
    body: String,
    engine: TestApplicationEngine,
    cookie: Cookie,
    numberOfExpectedViolations: Int) {
    val ex = assertFailsWith(ValidationException::class) {
        with(engine) {
            with(handleRequest(HttpMethod.Post, "/soknad") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
                setBody(body)
            }) {}
        }
    }

    assertEquals(numberOfExpectedViolations, ex.violations.size)

}

private fun body(
    fodselsdato: String? = "1990-09-25",
    fodselsnummer: String? = "25099012345",
    fraOgMed: String? = "2018-10-10",
    tilOgMed: String? = "2019-10-10",
    vedleggUrl: String) : String {

    val body = """{

	"barn": {
        "fornavn": "Santa",
		"mellomnavn": "Heisann",
		"etternavn": "winter",
		"relasjon": "mor",
		"fodselsnummer": "$fodselsnummer",
		"fodselsdato": "$fodselsdato"
	},
	"fra_og_med": "$fraOgMed",
	"til_og_med": "$tilOgMed",
	"ansettelsesforhold": {
        "organisasjoner": [
            {
                "organisasjonsnummer": "897895478",
		        "navn": "Bjeffefirmaet"
            }
        ]

	},
	"vedlegg": ["$vedleggUrl"]
    }""".trimIndent()
    logger.info(body)

    return body
}