package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.helse.getAuthCookie

class AuthMockCookieResponseTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val subject = request!!.queryParameter("subject").firstValue()
        val redirectLocation = if (request.queryParameter("redirect_location").isPresent) request.queryParameter("redirect_location").firstValue() else null
        val cookieName = if (request.queryParameter("cookie_name").isPresent) request.queryParameter("cookie_name").firstValue() else null
        val expiry = if (request.queryParameter("expiry").isPresent) request.queryParameter("expiry").firstValue().toLong() else null
        val issuer = if(request.queryParameter("issuer").isPresent) request.queryParameter("issuer").firstValue() else null
        val authenticationLevel = if(request.queryParameter("authentication_level").isPresent) request.queryParameter("authentication_level").firstValue() else null

        val cookie = getAuthCookie(
            fnr = subject,
            authLevel = authenticationLevel,
            cookieName = cookieName,
            expiry = expiry,
            issuer = issuer
        )

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