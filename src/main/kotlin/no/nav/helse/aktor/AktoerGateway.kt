package no.nav.helse.aktor

import io.ktor.client.HttpClient
import no.nav.helse.general.auth.Fodselsnummer
import java.net.URL

class AktorGateway(
    val httpClient: HttpClient,
    val baseUrl: URL
) {
    suspend fun getAktorId(fnr: Fodselsnummer) : AktorId {

    }
}