package no.nav.helse

import com.auth0.jwt.JWT
import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.Assert
import kotlin.test.assertContains

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

        internal fun List<String>.verifiserFeil(antallFeil: Int, valideringsfeil: List<String> = listOf()) {
            Assert.assertEquals(antallFeil, this.size)

            valideringsfeil.forEach {
                assertContains(this, it)
            }
        }

        internal fun List<String>.verifiserIngenFeil() {
            Assert.assertTrue(this.isEmpty())
        }
    }
}