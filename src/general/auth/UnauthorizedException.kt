package no.nav.pleiepenger.api.general.auth

import java.lang.RuntimeException

class UnauthorizedException : RuntimeException {
    constructor(throwable: Throwable) : super(throwable.message)
    constructor(message : String) : super(message)
}