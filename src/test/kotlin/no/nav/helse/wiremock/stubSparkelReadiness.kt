package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelReadiness() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/sparkel-mock/isready"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("READY")
            )
    )
}