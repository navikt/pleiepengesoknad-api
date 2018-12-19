package no.nav.helse.general.auth

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkException
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("nav.CookieAuthenticationProvider")


class CookieAuthenticationProvider(name: String?) : AuthenticationProvider(name) {
    internal var verifier : ((String)) -> JWTVerifier? = { null }
    internal var authenticationFunction: suspend ApplicationCall.(JWTCredential) -> Principal? = { null }
    internal var cookieName : String? = null

    fun verifier(jwkProvider: JwkProvider, issuer: String) {
        this.verifier = { token: String -> getVerifier(jwkProvider, issuer, token) }
    }

    fun validate(validate: suspend ApplicationCall.(JWTCredential) -> Principal?) {
        authenticationFunction = validate
    }

    fun withCookieName(cookieName: String) {
        this.cookieName = cookieName
    }
}


/**
 * Installs JWT Authentication mechanism
 */
fun Authentication.Configuration.jwtFromCookie(
    name: String? = null,
    configure: CookieAuthenticationProvider.() -> Unit
) {
    val provider = CookieAuthenticationProvider(name).apply(configure)
    val authenticate = provider.authenticationFunction
    val verifier = provider.verifier

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val cookie = call.request.cookies[provider.cookieName!!]
        if (cookie == null) {
            throw CookieNotSetException(provider.cookieName!!)
        } else {
            logger.debug(cookie)
            val principal = verifyAndValidate(call, verifier(cookie), cookie, authenticate) ?: throw IllegalStateException("principal == null")

            context.principal(principal)
            logger.debug("User successfully authorized")
        }
    }
    register(provider)
}

private suspend fun verifyAndValidate(
    call: ApplicationCall,
    jwtVerifier: JWTVerifier?,
    token: String,
    validate: suspend ApplicationCall.(JWTCredential) -> Principal?
): Principal? {
    val jwt = try {
        token?.let { jwtVerifier?.verify(it) }
    } catch (ex: Throwable) {
        logger.trace("Token verification failed: {}", ex.message)
        throw ex
    } ?: return null

    val payload = jwt.parsePayload()
    val credentials = JWTCredential(payload)
    return validate(call, credentials)
}

private fun DecodedJWT.parsePayload(): Payload {
    val payloadString = String(Base64.getUrlDecoder().decode(payload))
    return JWTParser().parsePayload(payloadString)
}

private fun getVerifier(
    jwkProvider: JwkProvider,
    issuer: String,
    token: String
): JWTVerifier? {
    try {
        val jwk = jwkProvider.get(JWT.decode(token).keyId)
        val algorithm = try {
            jwk.makeAlgorithm()
        } catch (cause: Throwable) {
            logger.trace(
                "Failed to create algorithm {}: {}",
                jwk.algorithm,
                cause.message ?: cause.javaClass.simpleName
            )
            throw cause
        }

        return JWT.require(algorithm).withIssuer(issuer).build()

    } catch (ex: JwkException) {
        logger.trace("Failed to get JWK: {}", ex.message)
        throw ex
    } catch (ex: JWTDecodeException) {
        logger.trace("Illegal JWT: {}", ex.message)
        throw ex
    }
}

private fun Jwk.makeAlgorithm(): Algorithm = when (algorithm) {
    "RS256" -> Algorithm.RSA256(publicKey as RSAPublicKey, null)
    "RS384" -> Algorithm.RSA384(publicKey as RSAPublicKey, null)
    "RS512" -> Algorithm.RSA512(publicKey as RSAPublicKey, null)
    "ES256" -> Algorithm.ECDSA256(publicKey as ECPublicKey, null)
    "ES384" -> Algorithm.ECDSA384(publicKey as ECPublicKey, null)
    "ES512" -> Algorithm.ECDSA512(publicKey as ECPublicKey, null)
    null -> Algorithm.RSA256(publicKey as RSAPublicKey, null)
    else -> throw IllegalArgumentException("Unsupported algorithm $algorithm")
}