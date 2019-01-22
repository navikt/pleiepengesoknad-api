package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response

class AktoerRegisterMockGetAktoerIdResponseTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val fnr = request!!.getHeader("Nav-Personidenter")

        return Response.Builder.like(response)
            .body(getResponse(fnr))
            .build()
    }

    override fun getName(): String {
        return "aktoer-register-mock-get-aktoer-id"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

}

private fun getResponse(fnr: String) = """
{
  "$fnr": {
    "identer": [
      {
        "ident": "12345",
        "identgruppe": "AktoerId",
        "gjeldende": true
      }
    ],
    "feilmelding": null
  }
}
""".trimIndent()