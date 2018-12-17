package no.nav.pleiepenger.api

import com.github.tomakehurst.wiremock.http.Cookie
import no.nav.security.oidc.test.support.JwtTokenGenerator

fun getAuthCookie(
    fnr: String,
    cookieName: String = "localhost-idtoken") : Cookie {
    val jwt = JwtTokenGenerator.createSignedJWT(fnr).serialize()
    return Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))
}