package no.nav.helse

import com.auth0.jwt.JWT
import com.github.tomakehurst.wiremock.http.Cookie
import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import org.junit.Assert

class TestUtils {
    companion object {
        fun getIdentFromIdToken(request: Request?): String {
            val idToken: String = request!!.getHeader(HttpHeaders.Authorization).substringAfter("Bearer ")
            return JWT.decode(idToken).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        }

        fun getAuthCookie(
            fnr: String,
            level: Int = 4,
            cookieName: String = "localhost-idtoken",
            expiry: Long? = null) : Cookie {

            val overridingClaims : Map<String, Any> = if (expiry == null) emptyMap() else mapOf(
                "exp" to expiry
            )

            val jwt = LoginService.V1_0.generateJwt(fnr = fnr, level = level, overridingClaims = overridingClaims)
            return Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))
        }

        internal fun MutableList<String>.validerFeil(antallFeil: Int) {
            Assert.assertEquals(antallFeil, this.size)
        }

        internal fun MutableList<String>.validerIngenFeil() {
            Assert.assertTrue(this.isEmpty())
        }
    }
}