package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.soknad.Periode
import java.time.Duration

data class ArbeidsUker(
    val periode: Periode,
    val timer: Duration,
    val prosentAvNormalt: String
)