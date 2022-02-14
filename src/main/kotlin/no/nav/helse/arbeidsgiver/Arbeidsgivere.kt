package no.nav.helse.arbeidsgiver

import java.time.LocalDate

data class ArbeidsgivereOppslagRespons (
    val arbeidsgivere: Arbeidsgivere
)

data class Arbeidsgivere (
    val organisasjoner: List<Organisasjon>,
    val privateArbeidsgivere: List<PrivatArbeidsgiver>?,
    val frilansoppdrag: List<Frilansoppdrag>?
)

class Organisasjon (
    val organisasjonsnummer: String,
    val navn: String?,
    val ansattFom: LocalDate? = null,
    val ansattTom: LocalDate? = null
)

data class PrivatArbeidsgiver (
    val offentligIdent: String,
    val ansattFom: LocalDate? = null,
    val ansattTom: LocalDate? = null
)

data class Frilansoppdrag (
    val type: String,
    val organisasjonsnummer: String? = null,
    val navn: String? = null,
    val offentligIdent: String? = null,
    val ansattFom: LocalDate? = null,
    val ansattTom: LocalDate? = null
)