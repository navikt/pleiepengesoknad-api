package no.nav.pleiepenger.api.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.security.oidc.test.support.JwtTokenGenerator

class AuthMockJwtResponseTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val subject = request!!.queryParameter("subject").firstValue()

        return Response.Builder.like(response)
            .body(JwtTokenGenerator.createSignedJWT(subject).serialize())
            .build()
    }

    override fun getName(): String {
        return "auth-mock-jwt-response-transformer"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}