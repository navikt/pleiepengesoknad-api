package no.nav.pleiepenger.api

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.security.oidc.test.support.JwtTokenGenerator

class AuthMockCookieResponseTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val subject = request!!.queryParameter("subject").firstValue()
        val redirectLocation = if (request.queryParameter("redirect_location").isPresent) request.queryParameter("redirect_location").firstValue() else null
        val cookieName = if (request.queryParameter("cookie_name").isPresent) request.queryParameter("cookie_name").firstValue() else "localhost-idtoken"
        val jwt = JwtTokenGenerator.createSignedJWT(subject).serialize()

        val cookie = Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))

        if (redirectLocation.isNullOrBlank()) {
            return Response.Builder.like(response)
                .headers(HttpHeaders(HttpHeader.httpHeader("Set-Cookie", cookie.toString())))
                .status(200)
                .body(cookie.toString())
                .build()
        } else {
            return Response.Builder.like(response)
                .headers(HttpHeaders(
                    HttpHeader.httpHeader("Location", redirectLocation),
                    HttpHeader.httpHeader("Set-Cookie", cookie.toString())
                ))
                .status(302)
                .body(cookie.toString())
                .build()
        }
    }

    override fun getName(): String {
        return "auth-mock-cookie-response-transformer"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}