package no.nav.helse.general.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import no.nav.helse.general.error.DefaultError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

/*
    In summary, a 401 Unauthorized response should be used for missing or bad authentication,
    and a 403 Forbidden response should be used afterwards, when the user is authenticated but
    isn’t authorized to perform the requested operation on the given resource.
 */

private val defaultType = URI.create("/errors/invalid-login")
private val loginExpiredType = URI.create("/errors/login-expired")
private val loginRequiredType = URI.create("/errors/login-required")
private val insufficientLevelType = URI.create("/errors/insufficient-authentication-level")


fun StatusPages.Configuration.authorizationStatusPages() {

    exception<JWTVerificationException> { cause ->
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = defaultType,
            title = cause.message ?: "Unable to verify login."
        ))
        throw cause
    }

    exception<TokenExpiredException> { cause ->
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginExpiredType,
            title = "The login has expired.",
            detail = cause.message
        ))
        throw cause
    }

    exception<InsufficientAuthenticationLevelException> { cause ->
        call.respond(HttpStatusCode.Forbidden, DefaultError(
            status = HttpStatusCode.Forbidden.value,
            type = insufficientLevelType,
            title = "Insufficient authentication level to perform request.",
            detail = cause.message
        ))
        throw cause
    }

    exception<CookieNotSetException> { cause ->
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginRequiredType,
            title = "Not logged in.",
            detail = cause.message
        ))
        throw cause
    }


}