package no.nav.helse.endringsmelding

import no.nav.helse.soker.Søker
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Endringsmelding(
    val søknadId: UUID = UUID.randomUUID(),
    val versjon: String = "1.0.0",
    val mottattDato: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val ytelse: PleiepengerSyktBarn,
)

data class KomplettEndringsmelding(
    val k9Format: Søknad,
    val søker: Søker
)
