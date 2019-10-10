package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders

internal fun WireMockServer.stubSparkelGetPerson(
    aktoerId : String = "10000000001"
) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*$sparkelPath/api/person/.\\d+"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(sparkelResponse(aktoerId))
            )
    )
    return this
}

private fun sparkelResponse(aktoerId: String) : String = """
    {
        "fdato": "1997-05-05",
        "statsborgerskap": "NOR",
        "mellomnavn": "TILKNYTTET",
        "etternavn": "FORELDER",
        "aktørId": "$aktoerId",
        "bostedsland": "NOR",
        "fornavn": "IKKE",
        "kjønn": "KVINNE",
        "status": "BOSA"
    }
""".trimIndent()