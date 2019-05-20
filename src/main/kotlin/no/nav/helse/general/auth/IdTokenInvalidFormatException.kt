package no.nav.helse.general.auth

import java.lang.RuntimeException

class IdTokenInvalidFormatException(idToken: IdToken, cause: Throwable? = null) : RuntimeException("$idToken er på ugyldig format.", cause)