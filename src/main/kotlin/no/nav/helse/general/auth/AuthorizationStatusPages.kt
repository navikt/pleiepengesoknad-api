package no.nav.helse.general.auth

import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.prometheus.client.Counter
import no.nav.helse.general.error.DefaultError
import no.nav.helse.general.error.monitorException
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

private val logger: Logger = LoggerFactory.getLogger("nav.authorizationStatusPages")


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
        throw cause // Dette burde ikke skje, kaster videre
    }

    exception<SigningKeyNotFoundException> { cause ->
        monitorException(cause, loginExpiredType, errorCounter)
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginExpiredType,
            title = "The login has expired.",
            detail = cause.message
        ))
        // Etterssom issuer har rullert nøkler vet vi at toknet uansett har expiret, så gir samme feilmelding som på expired token.
        logger.trace("Fant ikke nøkkelen på JWT url for å verifisere signatur på tokenet. Issuer har rullert nøkler siden dette tokenet ble issuet => Expired Token.")
    }

    exception<TokenExpiredException> { cause ->
        monitorException(cause, loginExpiredType, errorCounter)
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginExpiredType,
            title = "The login has expired.",
            detail = cause.message
        ))
        logger.trace("Tokenet er ikke lenger gylidig.")
    }

    exception<InsufficientAuthenticationLevelException> { cause ->
        monitorException(cause, insufficientLevelType, errorCounter)
        call.respond(HttpStatusCode.Forbidden, DefaultError(
            status = HttpStatusCode.Forbidden.value,
            type = insufficientLevelType,
            title = "Insufficient authentication level to perform request.",
            detail = cause.message
        ))
        // Kan ha vært logget inn på en annen NAV-tjeneste som ikke krever nivå 4 i forkant
        // og må nå logge inn på nytt. Trenger ikke kaste exception videre
        logger.trace(cause.message)
    }

    exception<CookieNotSetException> { cause ->
        monitorException(cause, loginRequiredType, errorCounter)
        call.respond(HttpStatusCode.Unauthorized, DefaultError(
            status = HttpStatusCode.Unauthorized.value,
            type = loginRequiredType,
            title = "Not logged in.",
            detail = cause.message
        ))
        // Har ingen cookie satt, heller ikke en feil, kaster ikke exception videre
        logger.trace(cause.message)
    }
}