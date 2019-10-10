package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.helse.general.rest.NavHeaders

class K9OppslagArbeidsgivereTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val personIdent = request!!.getHeader(NavHeaders.PersonIdenter)

        return Response.Builder.like(response)
            .body(getResponse(
                personIdent = personIdent
            ))
            .build()
    }

    override fun getName(): String {
        return "k9-oppslag-arbeidsgivere"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(personIdent: String): String {
    when(personIdent) {
        "02119970078" -> {
        return """
            {
                "organisasjoner": [{
                    "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                    "organisasjonsnummer": "913548221"
                }, {
                    "navn": "NAV, AVD WALDEMAR THRANES GATE",
                    "organisasjonsnummer": "984054564"
                }]
            }
        """.trimIndent()
        } else -> {
        return """
                {
                    "arbeidsgivere": []
                }
            """.trimIndent()
    }
    }
}