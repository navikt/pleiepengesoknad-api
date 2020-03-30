package no.nav.helse.ettersending

import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettEttersending (
    val soker: Soker,
    val sprak: String,
    val mottatt: ZonedDateTime,
    val vedlegg: List<Vedlegg>,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String,
    val soknadstype: String
)
