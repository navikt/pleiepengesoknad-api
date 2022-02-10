package no.nav.helse.endringsmelding

import no.nav.helse.soker.Søker
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.*

data class Endringsmelding(
    val søknadId: UUID? = UUID.randomUUID(),
    val mottattDato: ZonedDateTime? = ZonedDateTime.now(UTC),
    val språk: String,
    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val ytelse: PleiepengerSyktBarn,
)

data class KomplettEndringsmelding(
    val søker: Søker,
    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val k9Format: Søknad
)
