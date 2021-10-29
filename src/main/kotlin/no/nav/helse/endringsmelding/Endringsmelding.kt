package no.nav.helse.endringsmelding

import no.nav.helse.soker.Søker
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import java.util.*

data class Endringsmelding(
    val søknadId: UUID? = UUID.randomUUID(),
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
