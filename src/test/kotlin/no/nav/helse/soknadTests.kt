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
        val urlJpeg = handleRequestUploadImage(
            cookie = cookie,
            vedlegg = "vedlegg/nav-logo.png".fromResources(),
            fileName = "nav-logo.png",
            contentType = "image/png"
        )

        val urlPdf = handleRequestUploadImage(
            cookie = cookie,
            vedlegg = "vedlegg/test.pdf".fromResources(),
            fileName = "test.pdf",
            contentType = "application/pdf"
        )

        handleRequest(HttpMethod.Post, "/soknad") {
            addHeader("Accept", "application/json")
            addHeader("Cookie", cookie.toString())
            addHeader("Content-Type", "application/json")
            setBody(body(vedleggUrl1 = urlJpeg, vedleggUrl2 = urlPdf))
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
                addHeader("Content-Type", "application/json")
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
        body = body(fodselsnummer = "123", vedleggUrl1 = "http://localhost:8080/vedlegg/123123", vedleggUrl2 = "http://localhost:8080/vedlegg/123124"),
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
                addHeader("Content-Type", "application/json")
                setBody(body)
            }) {}
        }
    }

    assertEquals(numberOfExpectedViolations, ex.violations.size)

}

private fun body(
    fodselsnummer: String? = "25099012345",
    fraOgMed: String? = "2018-10-10",
    tilOgMed: String? = "2019-10-10",
    vedleggUrl1: String,
    vedleggUrl2: String) : String {

    val body = """
    {
        "barn": {
            "navn": "Santa ÆØÅ Winter",
            "fodselsnummer": "$fodselsnummer"
        },
        "relasjon_til_barnet": "mor",
        "fra_og_med": "$fraOgMed",
        "til_og_med": "$tilOgMed",
        "arbeidsgivere": {
            "organisasjoner": [
                {
                    "organisasjonsnummer": "897895478",
                    "navn": "Bjeffefirmaet"
                }
            ]
        },
        "vedlegg": [
            "$vedleggUrl1",
            "$vedleggUrl2"
        ],
        "medlemskap" : {
            "har_bodd_i_utlandet_siste_12_mnd" : false,
            "skal_bo_i_utlandet_neste_12_mnd" : true
	    }
    }
    """.trimIndent()
    logger.info(body)

    return body
}