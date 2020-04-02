package no.nav.helse.ettersending

import java.net.URL

data class Ettersending(
    val språk: String,
    val vedlegg: List<URL>,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String,
    val soknadstype: String
)
