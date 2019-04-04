package no.nav.helse

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.github.tomakehurst.wiremock.http.Cookie
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val logger: Logger = LoggerFactory.getLogger("nav.soknadValidationTest")

// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val gyldigFodselsnummerB = "19066672169"
private val gyldigFodselsnummerC = "20037473937"

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


private fun body(
    fodselsnummer: String? = gyldigFodselsnummerA,
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
                    "organisasjonsnummer": "917755736",
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