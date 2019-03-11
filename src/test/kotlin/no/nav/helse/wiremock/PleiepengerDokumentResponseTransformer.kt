package no.nav.helse.wiremock

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.*
import no.nav.helse.general.jackson.configureObjectMapper
import no.nav.helse.vedlegg.Vedlegg
import no.nav.helse.vedlegg.VedleggId
import java.util.*
import kotlin.IllegalStateException

class PleiepengerDokumentResponseTransformer() : ResponseTransformer() {

    val storage = mutableMapOf<VedleggId, Vedlegg>()
    val objectMapper = configureObjectMapper()

    override fun getName(): String {
        return "PleiepengerDokumentResponseTransformer"
    }

    override fun applyGlobally(): Boolean {
        return false
    }

    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        return when {
            request == null -> throw IllegalStateException("request == null")
            request.method == RequestMethod.GET -> {

                val vedleggId = request.getVedleggId()
                return if (storage.containsKey(vedleggId)) {
                    Response.Builder.like(response)
                        .status(200)
                        .headers(HttpHeaders(
                            HttpHeader.httpHeader("Content-Type", "application/json")
                        ))
                        .body(objectMapper.writeValueAsString(storage[vedleggId]))
                        .build()
                } else {
                    Response.Builder.like(response)
                        .status(404)
                        .build()
                }
            }

            request.method == RequestMethod.POST -> {
                val vedlegg = objectMapper.readValue<Vedlegg>(request.bodyAsString)
                val vedleggId = VedleggId(UUID.randomUUID().toString())
                storage[vedleggId] = vedlegg
                Response.Builder.like(response)
                    .status(201)
                    .headers(HttpHeaders(
                        HttpHeader.httpHeader("Location", "http://localhost:8080/v1/dokument/${vedleggId.value}"),
                        HttpHeader.httpHeader("Content-Type", "application/json")
                    ))
                    .body("""
                        {
                            "id" : "${vedleggId.value}"
                        }
                    """.trimIndent())
                    .build()

            }
            request.method == RequestMethod.DELETE -> {
                val vedleggId = request.getVedleggId()
                if (storage.containsKey(vedleggId)) {
                    storage.remove(vedleggId)
                    Response.Builder.like(response)
                        .status(204)
                        .build()
                } else {
                    Response.Builder.like(response)
                        .status(404)
                        .build()
                }
            }
            else -> throw IllegalStateException("Uventet request.")
        }
    }
}

private fun Request.getVedleggId() : VedleggId = VedleggId(url.substringAfterLast("/"))
