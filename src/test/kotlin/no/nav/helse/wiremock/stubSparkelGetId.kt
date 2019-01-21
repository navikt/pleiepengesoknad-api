package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import java.util.*

fun stubSparkelgetId() {
    val uuid = UUID.randomUUID().toString()
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/sparkel-mock/api/ident.*"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":\"$uuid\"}")
            )
    )
}