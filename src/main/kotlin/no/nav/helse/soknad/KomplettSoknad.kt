package no.nav.helse.soknad

import no.nav.helse.soker.Soker
import java.time.LocalDate

data class KomplettSoknad(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val soker : Soker,
    val barn : List<BarnDetaljer>,
    val ansettelsesforhold: List<AnsettelsesforholdDetaljer>,
    val vedlegg: List<PdfVedlegg>
)