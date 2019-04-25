package no.nav.helse.general.auth

import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*
    In summary, a 401 Unauthorized response should be used for missing or bad authentication,
    and a 403 Forbidden response should be used afterwards, when the user is authenticated but
    isn’t authorized to perform the requested operation on the given resource.

    Når https://github.com/ktorio/ktor/issues/1048 er i en release kan mye av denne koden fjernes.
    Da vil ikke det være noen response entity, kun 401 responser, endrer til dette for å unngå brudd av api når
 */

private val logger: Logger = LoggerFactory.getLogger("nav.authorizationStatusPages")


fun StatusPages.Configuration.authorizationStatusPages() {

    exception<JWTVerificationException> { cause ->
        call.respond(HttpStatusCode.Unauthorized)
        // Dette Scenarioet bør ikke forekomme i en normal setting
        logger.error("Klarte ikke å verifiere JWT", cause)
    }

    exception<SigningKeyNotFoundException> {
        call.respond(HttpStatusCode.Unauthorized)
        // Etterssom issuer har rullert nøkler vet vi at toknet uansett har expiret, så gir samme feilmelding som på expired token.
        logger.trace("Fant ikke nøkkelen på JWT url for å verifisere signatur på tokenet. Issuer har rullert nøkler siden dette tokenet ble issuet => Expired Token.")
    }

    exception<TokenExpiredException> {
        call.respond(HttpStatusCode.Unauthorized)
        logger.trace("Tokenet er ikke lenger gylidig.")
    }

    exception<InsufficientAuthenticationLevelException> { cause ->
        call.respond(HttpStatusCode.Forbidden)
        // Kan ha vært logget inn på en annen NAV-tjeneste som ikke krever nivå 4 i forkant
        // og må nå logge inn på nytt. Trenger ikke å logge noe error på dette
        logger.trace(cause.message)
    }

    exception<CookieNotSetException> { cause ->
        call.respond(HttpStatusCode.Unauthorized)
        // Har ingen cookie satt, heller ikke en feil
        logger.trace(cause.message)
    }
}