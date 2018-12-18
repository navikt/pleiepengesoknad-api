package no.nav.pleiepenger.api.general.error

import io.ktor.client.response.HttpResponse
import io.ktor.http.Url
import java.lang.RuntimeException

class CommunicationException : RuntimeException {

    constructor(requestedUrl: Url,
                httpResponse: HttpResponse) : super(String.format("Error requesting '%s', got response '%s'", requestedUrl, httpResponse))

    constructor(requestedUrl: Url,
                cause: Throwable) : super(String.format("Error requesting '%s', got exception", requestedUrl), cause)
}