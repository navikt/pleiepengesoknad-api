package no.nav.helse.arbeidsgiver

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.general.CallId
import no.nav.helse.general.oppslag.K9OppslagGateway
import no.nav.helse.general.oppslag.throwable
import no.nav.helse.k9SelvbetjeningOppslagKonfigurert
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ArbeidsgivereGateway(
    baseUrl: URI
) : K9OppslagGateway(baseUrl) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger("nav.ArbeidsgivereGateway")
        private const val HENTE_ARBEIDSGIVERE_OPERATION = "hente-arbeidsgivere"
        private const val HENTE_ARBEIDSGIVERE_MED_ORGANISASJONSNUMMER_OPERATION =
            "hente-arbeidsgivere-med-organisasjonsnummer"
        private val objectMapper = jacksonObjectMapper().k9SelvbetjeningOppslagKonfigurert()

        private val arbeidsgivereAttributter = listOf(
            "arbeidsgivere[].organisasjoner[].organisasjonsnummer",
            "arbeidsgivere[].organisasjoner[].navn",
            "arbeidsgivere[].organisasjoner[].ansettelsesperiode"
        )

        private val privateArbeidsgivereAttributter = listOf(
            "private_arbeidsgivere[].ansettelsesperiode",
            "private_arbeidsgivere[].offentlig_ident"
        )

        private val frilansoppdragAttributter = listOf("frilansoppdrag[]")
    }

    internal suspend fun hentArbeidsgivere(
        idToken: IdToken,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean
    ): Arbeidsgivere {
        var attributter = Pair("a", mutableListOf<String>())

        attributter.second.addAll(arbeidsgivereAttributter)
        if(skalHentePrivateArbeidsgivere) attributter.second.addAll(privateArbeidsgivereAttributter)
        if(skalHenteFrilansoppdrag) attributter.second.addAll(frilansoppdragAttributter)

        val arbeidsgivereUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                attributter,
                Pair("fom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))),
                Pair("tom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed)))
            )
        ).toString()

        val httpRequest = generateHttpRequest(idToken, arbeidsgivereUrl, callId)

        val arbeidsgivereOppslagRespons = Retry.retry(
            operation = HENTE_ARBEIDSGIVERE_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_ARBEIDSGIVERE_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<ArbeidsgivereOppslagRespons>(success) },
                { error ->
                    logger.error(
                        "Error response = '${
                            error.response.body().asString("text/plain")
                        }' fra '${request.url}'"
                    )
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av arbeidsgiver.")
                }
            )
        }
        return arbeidsgivereOppslagRespons.arbeidsgivere
    }

    suspend fun hentArbeidsgivere(
        idToken: IdToken,
        callId: CallId,
        organisasjoner: Set<Organisasjonsnummer>
    ): Arbeidsgivere {
        val arbeidsgivereUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("arbeidsgivere"),
            queryParameters = mapOf(
                Pair("a", arbeidsgivereAttributter),
                Pair("org", organisasjoner.map { it.verdi }),
            )
        ).toString()

        val httpRequest = generateHttpRequest(idToken, arbeidsgivereUrl, callId)

        val arbeidsgivereOppslagRespons = Retry.retry(
            operation = HENTE_ARBEIDSGIVERE_MED_ORGANISASJONSNUMMER_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "pleiepengesoknad-api",
                operation = HENTE_ARBEIDSGIVERE_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<ArbeidsgivereOppslagRespons>(success) },
                { error -> throw error.throwable(request, logger, "Feil ved henting av arbeidsgiver.") }
            )
        }
        return arbeidsgivereOppslagRespons.arbeidsgivere
    }
}