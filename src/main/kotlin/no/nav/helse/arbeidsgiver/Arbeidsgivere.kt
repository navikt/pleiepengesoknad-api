package no.nav.helse.arbeidsgiver

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class ArbeidsgivereOppslagRespons (
    val arbeidsgivere: Arbeidsgivere
)

data class Arbeidsgivere (
    val organisasjoner: List<Organisasjon>,
    @JsonAlias("private_arbeidsgivere") val privateArbeidsgivere: List<PrivatArbeidsgiver>
)

class Organisasjon (
    val organisasjonsnummer: String,
    val navn: String?
)

data class PrivatArbeidsgiver (
    @JsonAlias("offentlig_ident") val offentligIdent: String,
    @JsonAlias("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonAlias("ansatt_tom") val ansattTom: LocalDate? = null
)