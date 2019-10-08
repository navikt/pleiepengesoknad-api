package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response

class K9OppslagBarnTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val personIdent = request!!.getHeader("Nav-Personidenter")

        return Response.Builder.like(response)
            .body(getResponse(
                personIdent = personIdent
            ))
            .build()
    }

    override fun getName(): String {
        return "k9-oppslag-barn"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(personIdent: String): String {
    when(personIdent) {
        "290990123456" -> {
            return """
            {
                "barn": [{
                    "fødselsdato": "2000-08-27",
                    "fornavn": "BARN",
                    "mellomnavn": "EN",
                    "etternavn": "BARNESEN",
                    "aktør_id": "1000000000001"
                }, {
                    "fødselsdato": "2001-04-10",
                    "fornavn": "BARN",
                    "mellomnavn": "TO",
                    "etternavn": "BARNESEN",
                    "aktør_id": "1000000000002"
                }]
            }
            """.trimIndent()
        } else -> {
            return """
                {}
            """.trimIndent()
        }
    }
}