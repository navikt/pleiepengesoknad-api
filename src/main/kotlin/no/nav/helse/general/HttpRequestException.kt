package no.nav.helse.general

import no.nav.helse.general.error.getRootCause

class HttpRequestException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause.getRootCause())
}
