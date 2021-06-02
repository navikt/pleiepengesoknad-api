package no.nav.helse.vedlegg

import com.fasterxml.jackson.annotation.JsonProperty

data class DokumentEier(
    @JsonProperty("eiers_fødselsnummer") val eiersFødselsnummer: String
)