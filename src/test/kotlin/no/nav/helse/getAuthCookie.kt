package no.nav.helse

import com.github.tomakehurst.wiremock.http.Cookie
import no.nav.security.oidc.test.support.JwtTokenGenerator

fun getAuthCookie(
    fnr: String,
    issuer: String? = null,
    authLevel: String? = null,
    cookieName: String? = null,
    expiry: Long? = null) : Cookie {

    val claimSet = JwtTokenGenerator.buildClaimSet(
        fnr,
        issuer ?: JwtTokenGenerator.ISS,
        JwtTokenGenerator.AUD,
        authLevel ?: JwtTokenGenerator.ACR,
        expiry ?: JwtTokenGenerator.EXPIRY
    )

    val jwt = JwtTokenGenerator.createSignedJWT(claimSet).serialize()
    return Cookie(listOf(String.format("%s=%s", cookieName ?: "localhost-idtoken", jwt), "Path=/", "Domain=localhost"))
}