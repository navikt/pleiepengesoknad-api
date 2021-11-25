package no.nav.helse.arbeidsgiver

import java.time.LocalDate

data class ArbeidsgivereOppslagRespons (
    val arbeidsgivere: Arbeidsgivere
)

data class Arbeidsgivere (
    val organisasjoner: List<Organisasjon>,
    val private_arbeidsgivere: List<PrivatArbeidsgiver>
)

class Organisasjon (
    val organisasjonsnummer: String,
    val navn: String?
)

data class PrivatArbeidsgiver (
    val offentlig_ident: String,
    val ansatt_fom: LocalDate? = null,
    val ansatt_tom: LocalDate? = null
)