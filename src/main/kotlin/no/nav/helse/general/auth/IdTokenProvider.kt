package no.nav.helse.general.auth

import io.ktor.application.ApplicationCall
import io.ktor.http.auth.HttpAuthHeader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdTokenProvider(
    private val cookieName : String
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger("nav.IdTokenProvider")
    }

    fun getIdToken(call: ApplicationCall) : IdToken {
        val cookie = call.request.cookies[cookieName] ?: throw CookieNotSetException(cookieName)
        return IdToken(value = cookie)
    }

    fun getIdTokenAsHttpAuthHeaderOrNull(call : ApplicationCall) : HttpAuthHeader? {
        val idToken = getIdToken(call)
        return try {
            HttpAuthHeader.Single("Bearer", idToken.value)
        } catch (cause: Throwable) {
            logger.error("Ugyldig ID-Token '${idToken.value}'", cause)
            null
        }
    }
}