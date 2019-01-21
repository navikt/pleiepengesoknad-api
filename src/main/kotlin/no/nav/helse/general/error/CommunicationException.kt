package no.nav.helse.general.error

import io.ktor.client.response.HttpResponse
import java.lang.RuntimeException
import java.net.URL

class CommunicationException : RuntimeException {

    constructor(requestedUrl: URL,
                httpResponse: HttpResponse) : super(String.format("Error requesting '%s', got response '%s'", requestedUrl, httpResponse))

    constructor(requestedUrl: URL,
                cause: Throwable) : super(String.format("Error requesting '%s', got exception", requestedUrl), cause)
}