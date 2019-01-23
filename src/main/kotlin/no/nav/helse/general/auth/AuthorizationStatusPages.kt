package no.nav.helse.general.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.prometheus.client.Counter
import no.nav.helse.general.error.DefaultError
import no.nav.helse.general.error.monitorException
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


fun StatusPages.Configuration.authorizationStatusPages(
    errorCounter: Counter
) {

    exception<JWTVerificationException> { cause ->
        monitorException(cause, defaultType, errorCounter)
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = defaultType,
            title = cause.message ?: "Unable to verify login."
        ))
        throw cause
    }

    exception<TokenExpiredException> { cause ->
        monitorException(cause, loginExpiredType, errorCounter)
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginExpiredType,
            title = "The login has expired.",
            detail = cause.message
        ))
        throw cause
    }

    exception<InsufficientAuthenticationLevelException> { cause ->
        monitorException(cause, insufficientLevelType, errorCounter)
        call.respond(HttpStatusCode.Forbidden, DefaultError(
            status = HttpStatusCode.Forbidden.value,
            type = insufficientLevelType,
            title = "Insufficient authentication level to perform request.",
            detail = cause.message
        ))
        throw cause
    }

    exception<CookieNotSetException> { cause ->
        monitorException(cause, loginRequiredType, errorCounter)
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginRequiredType,
            title = "Not logged in.",
            detail = cause.message
        ))
        throw cause
    }


}