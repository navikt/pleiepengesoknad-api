package no.nav.helse.ettersending

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

data class Ettersending(
    @JsonAlias("språk", "sprak") //TODO fjerne når frontend er oppdatert
    val sprak: String,
    val vedlegg: List<URL>,
    @JsonAlias("harForståttRettigheterOgPlikter", "harForstattRettigheterOgPlikter") //TODO Fjernes når frontend er oppdatert
    val harForstattRettigheterOgPlikter: Boolean,
    @JsonProperty("harBekreftetOpplysninger")
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String,
    @JsonAlias("søknadstype", "soknadstype") //TODO fjerne når frontend er oppdatert
    val soknadstype: String
)
