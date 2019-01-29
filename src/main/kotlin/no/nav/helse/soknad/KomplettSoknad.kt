package no.nav.helse.soknad

import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate

data class KomplettSoknad(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val soker : Soker,
    val barn : BarnDetaljer,
    val ansettelsesforhold: AnsettelsesforholdDetaljer,
    val vedlegg: List<Vedlegg>
)