package no.nav.helse.ettersending

import com.fasterxml.jackson.annotation.JsonAlias
import java.net.URL

data class Ettersending(
    @JsonAlias("språk", "sprak") //TODO fjerne når frontend er oppdatert
    val sprak: String,
    val vedlegg: List<URL>,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String,
    @JsonAlias("søknadstype", "soknadstype") //TODO fjerne når frontend er oppdatert
    val soknadstype: String
)
