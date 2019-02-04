package no.nav.helse.soknad

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Max
import javax.validation.constraints.Pattern
import kotlin.math.max

@ValidBarnDetaljer
data class BarnDetaljer(
    @get:Pattern(regexp = "\\d{11}") val fodselsnummer: String?,
    @get:Pattern(regexp = "\\d{11}") val alternativId: String?,
    @get:Length(max = 100) val navn: String
)