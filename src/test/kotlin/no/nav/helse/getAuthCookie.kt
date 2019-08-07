package no.nav.helse

import com.github.tomakehurst.wiremock.http.Cookie
import no.nav.helse.dusseldorf.ktor.testsupport.jws.LoginService

fun getAuthCookie(
    fnr: String,
    level: Int = 4,
    cookieName: String = "localhost-idtoken",
    expiry: Long? = null) : Cookie {

    val jwt = LoginService.V1_0.generateJwt(fnr = fnr, level = level)
    return Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))
}