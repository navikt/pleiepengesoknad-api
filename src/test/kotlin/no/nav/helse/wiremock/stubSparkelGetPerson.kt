package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders

internal fun WireMockServer.stubSparkelGetPerson(
    fodselsdato : String = "1997-05-25"
) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*$sparkelPath/api/person/.\\d+"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(sparkelResponse(fodselsdato))
            )
    )
    return this
}

private fun sparkelResponse(fodselsdato: String) : String = """
    {
        "fdato": "$fodselsdato",
        "statsborgerskap": "NOR",
        "mellomnavn": "HEISANN",
        "etternavn": "MORSEN",
        "aktørId": "1060877738241",
        "bostedsland": "NOR",
        "fornavn": "MOR",
        "kjønn": "KVINNE",
        "status": "BOSA"
    }
""".trimIndent()

fun expectedGetSokerJson(
    fodselsnummer: String,
    fodselsdato: String = "1997-05-25",
    myndig : Boolean = true) = """
    {
        "etternavn": "MORSEN",
        "fornavn": "MOR",
        "mellomnavn": "HEISANN",
        "fodselsnummer": "$fodselsnummer",
        "aktoer_id": "12345",
        "fodselsdato": "$fodselsdato",
        "myndig": $myndig
    }
""".trimIndent()

