package no.nav.helse.ettersending

import no.nav.helse.soker.Søker
import no.nav.helse.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettEttersending (
    val soker: Søker,
    val sprak: String,
    val mottatt: ZonedDateTime,
    val vedlegg: List<Vedlegg>,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String,
    val soknadstype: String
)
