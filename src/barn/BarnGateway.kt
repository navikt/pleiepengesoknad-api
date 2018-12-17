package no.nav.pleiepenger.api.barn

import io.ktor.client.HttpClient
import io.ktor.http.Url
import no.nav.pleiepenger.api.id.Id
import java.time.LocalDate

class BarnGateway(
    httpClient: HttpClient,
    baseUrl: Url
) {
    fun getBarn(id: Id) : List<Barn>{
        return listOf(
            Barn(
                fornavn = "Barn",
                mellomnavn = "Barn",
                etternavn = "Barnesen",
                fodselsdato = LocalDate.now()
            )
        )
    }
}