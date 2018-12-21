package no.nav.helse.soknad

import javax.validation.constraints.NotBlank

data class AnsettelsesforholdDetaljer(
    @get:NotBlank val navn: String
)