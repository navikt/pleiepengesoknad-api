package no.nav.helse.ettersending

import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettEttersending (
    val søker: Soker,
    val språk: String,
    val mottatt: ZonedDateTime,
    val vedlegg: List<Vedlegg>,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String,
    val søknadstype: String
)
