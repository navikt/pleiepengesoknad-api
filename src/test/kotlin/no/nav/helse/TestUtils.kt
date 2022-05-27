package no.nav.helse

import com.auth0.jwt.JWT
import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.Assert

class TestUtils {
    companion object {
        fun getIdentFromIdToken(request: Request?): String {
            val idToken: String = request!!.getHeader(HttpHeaders.Authorization).substringAfter("Bearer ")
            return JWT.decode(idToken).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        }

        fun MockOAuth2Server.issueToken(
            fnr: String,
            issuerId: String = "tokendings",
            audience: String = "dev-gcp:dusseldorf:pleiepengesoknad-api",
            claims: Map<String, String> = mapOf("acr" to "Level4"),
            cookieName: String = "selvbetjening-idtoken",
            somCookie: Boolean = false,
        ): String {
            val jwtToken =
                issueToken(issuerId = issuerId, subject = fnr, audience = audience, claims = claims, expiry = 36000).serialize()
            return when (somCookie) {
                false -> jwtToken
                true -> "$cookieName=$jwtToken"
            }
        }

        internal fun MutableList<String>.validerFeil(antallFeil: Int) {
            Assert.assertEquals(antallFeil, this.size)
        }

        internal fun MutableList<String>.validerIngenFeil() {
            Assert.assertTrue(this.isEmpty())
        }
    }
}