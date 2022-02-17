package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.helse.TestUtils

class ArbeidsgivereMedPrivateResponseTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        return Response.Builder.like(response)
            .body(getResponse(
                ident = TestUtils.getIdentFromIdToken(request)
            ))
            .build()
    }

    override fun getName(): String {
        return "k9-oppslag-arbeidsgivere-med-private"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(ident: String): String {
    when (ident) {
        "02119970078" -> {
            //language=json
            return """
            {
              "arbeidsgivere": {
                "organisasjoner": [
                  {
                    "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ",
                    "organisasjonsnummer": "913548221"
                  },
                  {
                    "navn": "NAV, AVD WALDEMAR THRANES GATE",
                    "organisasjonsnummer": "984054564"
                  }
                ],
                "private_arbeidsgivere": [
                  {
                    "offentlig_ident": "10047206508",
                    "ansatt_fom": "2014-07-01",
                    "ansatt_tom": "2015-12-31"
                  }
                ],
                "frilansoppdrag": [
                  {
                    "type": "Person",
                    "ansatt_fom": "2020-01-01",
                    "ansatt_tom": "2022-02-28",
                    "offentlig_ident": "805824352"
                  },
                  {
                    "type": "Organisasjon",
                    "ansatt_fom": "2020-01-01",
                    "ansatt_tom": "2022-02-28",
                    "organisasjonsnummer": "123456789",
                    "navn": "DNB, FORSIKRING"
                  }
                ]
              }
            }
            """.trimIndent()
        }
        else -> {
            return """
                {
                    "arbeidsgivere": {
                        "organisasjoner": [],
                        "private_arbeidsgivere" : []
                    }
                }
            """.trimIndent()
        }
    }
}