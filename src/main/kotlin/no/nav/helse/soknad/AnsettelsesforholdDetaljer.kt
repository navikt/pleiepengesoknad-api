package no.nav.helse.soknad

import no.nav.helse.ansettelsesforhold.Ansettelsesforhold
import javax.validation.constraints.NotEmpty

data class AnsettelsesforholdDetaljer(
    @get:NotEmpty val organisasjoner : List<Ansettelsesforhold>
)