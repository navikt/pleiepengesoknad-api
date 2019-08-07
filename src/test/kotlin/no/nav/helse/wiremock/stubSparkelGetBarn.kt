package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders

internal fun WireMockServer.stubSparkelGetBarn(
    harBarn : Boolean = true
) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*$sparkelPath/api/person/.\\d+/barn"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(if (harBarn) sparkelResponseHarBarn else sparkelResponseIngenBarn)
            )
    )
    return this
}

private val sparkelResponseIngenBarn = """
{
    "barn": []
}
""".trimIndent()

private val sparkelResponseHarBarn = """
{
    "barn": [
        {
            "fdato": "2000-08-27",
            "statsborgerskap": "NOR",
            "mellomnavn": "EN",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000001",
            "fornavn": "BARN",
            "kjønn": "MANN",
            "status": "BOSA"
        },
        {
            "fdato": "2001-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "TO",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000002",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "FØDR",
            "diskresjonskode": "UFB"
        },
        {
            "fdato": "2010-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "TRE",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000003",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "DØD"
        },
        {
            "fdato": "2010-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "FIRE",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000004",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "DØD"
        },
        {
            "fdato": "2016-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "FEM",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000005",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "BOSA",
            "diskresjonskode": "SPSF"
        },
        {
            "fdato": "2016-04-12",
            "statsborgerskap": "NOR",
            "mellomnavn": "SEKS",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000006",
            "fornavn": "BARN",
            "kjønn": "MANN",
            "status": "BOSA",
            "diskresjonskode": "SPFO"
        }
    ]
}
""".trimIndent()