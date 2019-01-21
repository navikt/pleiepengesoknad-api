package no.nav.helse

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
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
    cookie: String
) {
    with(engine) {
        with(handleRequest(HttpMethod.Post, "/soknad") {
            addHeader("Accept", "application/json")
            addHeader("Cookie", cookie)
            setBody(body())
        }) {
            assertEquals(HttpStatusCode.Accepted, response.status())
        }
    }
}


fun obligatoriskeFelterIkkeSatt(
    engine: TestApplicationEngine,
    cookie: String) {
    assertFailsWith(MissingKotlinParameterException::class) {
        with(engine) {
            with(handleRequest(HttpMethod.Post, "/soknad") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie)
                setBody("{}")
            }) {}
        }
    }
}

fun ugyldigInformasjonOmBarn(
    engine: TestApplicationEngine,
    cookie: String) {

    expectViolationException(
        engine = engine,
        cookie = cookie,
        body = body(fodselsnummer = "123"),
        numberOfExpectedViolations = 1
    )

}

private fun expectViolationException(
    body: String,
    engine: TestApplicationEngine,
    cookie: String,
    numberOfExpectedViolations: Int) {
    val ex = assertFailsWith(ValidationException::class) {
        with(engine) {
            with(handleRequest(HttpMethod.Post, "/soknad") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie)
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
    tilOgMed: String? = "2019-10-10") : String {

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
	"vedlegg": [{
        "innhold": "${base64Vedlegg()}"
	}]
    }""".trimIndent()
    logger.info(body)

    return body
}