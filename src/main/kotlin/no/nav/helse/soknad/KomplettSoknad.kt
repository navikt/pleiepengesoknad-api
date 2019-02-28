package no.nav.helse.soknad

import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate

data class KomplettSoknad(
    val mottatt : LocalDate,
    val fraOgMed : LocalDate,
    val tilOgMed : LocalDate,
    val soker : Soker,
    val barn : BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetailjer,
    val vedlegg: List<Vedlegg>,
    val medlemskap : Medlemskap,
    val relasjonTilBarnet : String
)