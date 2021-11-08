package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.helse.TestUtils
import no.nav.helse.arbeidsgiver.orgQueryName
import org.slf4j.LoggerFactory

class ArbeidsgivereResponseTransformer : ResponseTransformer() {
    private companion object {
        private val logger = LoggerFactory.getLogger(ArbeidsgivereResponseTransformer::class.java)
    }

    override fun transform(
        request: Request,
        response: Response,
        files: FileSource?,
        parameters: Parameters?
    ): Response {

        val orgnummere = try {
            val values = request.queryParameter(orgQueryName).values()
            logger.info("Etterspurt organisasjonsnummer: {}", values)
            values
        } catch (ex: Exception) {
            null
        }

        return Response.Builder.like(response)
            .body(
                getResponse(
                    ident = TestUtils.getIdentFromIdToken(request),
                    organisasjonsnummere = orgnummere
                )
            )
            .build()
    }

    override fun getName(): String {
        return "k9-oppslag-arbeidsgivere"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(ident: String, organisasjonsnummere: List<String>?): String {
    when {
        organisasjonsnummere.isNullOrEmpty() -> when (ident) {
            "02119970078" -> {
                //language=json
                return """
            {
                "arbeidsgivere": {
                    "organisasjoner": [{
                        "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                        "organisasjonsnummer": "913548221",
                        "ansatt_fom": "2011-09-03",
                        "ansatt_tom": "2012-06-30"
                    }, {
                        "navn": "NAV, AVD WALDEMAR THRANES GATE",
                        "organisasjonsnummer": "984054564",
                        "ansatt_fom": "2011-09-03"
                    }]
                }
            }
        """.trimIndent()
            }
            else -> {
                //language=json
                return """
                {
                    "arbeidsgivere": {
                        "organisasjoner": []
                    }
                }
            """.trimIndent()
            }
        }
        else ->
            //language=json
            return """
                {
                    "arbeidsgivere": {
                        "organisasjoner": ${organisasjonsnummere.map { org(it) }}
                    }
                }
            """.trimIndent()
    }
}

private fun org(org: String) = when (org) {
    //language=json
    "977302390" -> """
          {
            "navn": "INMETA CONSULTING AS",
            "organisasjonsnummer": "977302390"
          }""".trimIndent()

    //language=json
    "984054564" -> """
        {
            "navn": "NAV, AVD WALDEMAR THRANES GATE",
            "organisasjonsnummer": "984054564"
        }
    """.trimIndent()

    //language=json
    "995784637" -> """
        {
            "navn": null,
            "organisasjonsnummer": "995784637"
        }
    """.trimIndent()

    else -> ""
}

