package no.nav.helse.general.auth

import com.auth0.jwt.JWT
import io.ktor.http.auth.HttpAuthHeader

data class IdToken(val value: String) {
    private val jwt = try {
        JWT.decode(value)
    } catch (cause: Throwable) {
        throw IdTokenInvalidFormatException(this, cause)
    }

    fun medValidertLevel(required: String) : IdToken {
        val acr = jwt.getClaim("acr")
        if (acr?.asString() == null) throw IdTokenInvalidFormatException(this)
        return if (required == acr.asString()) this
        else throw InsufficientAuthenticationLevelException(actualAcr = acr.asString(), requiredAcr = required)
    }

    fun somHttpAuthHeader() : HttpAuthHeader = HttpAuthHeader.Single("Bearer", value)
}