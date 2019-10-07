package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response

class K9OppslagResponseTransformer : ResponseTransformer() {
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
        return "k9-oppslag-soker"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(personIdent: String): String {
    when(personIdent) {
        "25037139184" -> {
            return """
        { 
            "aktør_id": "23456",
            "fornavn": "ARNE",
            "mellomnavn": "BJARNE",
            "etternavn": "CARLSEN",
            "fødselsdato": "1990-01-02"
        }
        """.trimIndent()
        } "290990123456" -> {
        return """
        {
            "etternavn": "MORSEN",
            "fornavn": "MOR",
            "mellomnavn": "HEISANN",
            "aktør_id": "12345",
            "fødselsdato": "1997-05-25"
        }
    """.trimIndent()
        }else -> {
            return """
                {}
            """.trimIndent()
        }
    }
}