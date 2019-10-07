package no.nav.helse.k9

import java.time.LocalDate

class PersonInfo (
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val foedselsdato: LocalDate,
    val barn: List<Barn>,
    val arbeidsgivere: List<Arbeidsgivere>
)