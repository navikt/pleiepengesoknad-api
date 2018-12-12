package no.nav.pleiepenger.api.soknad

import java.time.ZonedDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Past

data class Soknad (
    @get:Past
    val dato: ZonedDateTime,
    @get:NotEmpty
    val megatest: String,
    val endaen: String
)