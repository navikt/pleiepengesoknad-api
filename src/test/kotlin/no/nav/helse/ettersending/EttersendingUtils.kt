package no.nav.helse.ettersending

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.soker.Søker
import no.nav.helse.vedlegg.Vedlegg
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.ZonedDateTime

class EttersendingUtils {

    companion object {
        internal val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)

        private val gyldigFodselsnummerA = "02119970078"

        internal val default = Ettersending(
            sprak = "no",
            soknadstype = "pleiepenger",
            beskrivelse = "Beskrivelse av ettersending",
            vedlegg = listOf(
                URL("http://localhost:8080/vedlegg/1"),
                URL("http://localhost:8080/vedlegg/2"),
                URL("http://localhost:8080/vedlegg/3")
            ),
            harForstattRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )

        private val content =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z/C/HgAGgwJ/lK3Q6wAAAABJRU5ErkJggg==".toByteArray(
                Charset.defaultCharset()
            )

        internal val defaultKomplett = KomplettEttersending(
            mottatt = ZonedDateTime.now(),
            sprak = "no",
            soknadstype = "omsorgspenger",
            beskrivelse = "Beskrivelse av ettersending",
            soker = Søker(
                aktørId = "123456",
                fødselsdato = LocalDate.now().minusYears(25),
                fødselsnummer = gyldigFodselsnummerA,
                fornavn = "Ola",
                etternavn = "Normann"
            ),
            vedlegg = listOf(
                Vedlegg(
                    title = "Fil 1",
                    contentType = "img/png",
                    content = content
                ),
                Vedlegg(
                    title = "Fil 2",
                    contentType = "img/png",
                    content = content
                ),
                Vedlegg(
                    title = "Fil 3",
                    contentType = "img/png",
                    content = content
                )
            ),
            harForstattRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )
    }
}

internal fun Ettersending.somJson() = EttersendingUtils.objectMapper.writeValueAsString(this)
internal fun KomplettEttersending.somJson() = EttersendingUtils.objectMapper.writeValueAsString(this)
